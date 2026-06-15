<template>
  <div class="discussion-center">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          <i class="el-icon-chat-dot-square" />
          交流中心
        </h1>
        <p class="page-subtitle">分享知识，交流经验，共同进步</p>
      </div>
      <el-button type="primary" icon="el-icon-edit" @click="handleCreateDiscussion">
        发布话题
      </el-button>
    </div>

    <div v-if="announcements.length > 0" class="announcement-bar">
      <i class="el-icon-bell" />
      <div class="announce-content">
        <transition name="slide" mode="out-in">
          <span :key="currentAnnouncementIndex" class="announce-text" @click="showAnnouncementDetail(announcements[currentAnnouncementIndex])">
            {{ announcements[currentAnnouncementIndex].title }}
          </span>
        </transition>
      </div>
      <div class="announce-controls">
        <i class="el-icon-arrow-left" @click="prevAnnouncement" />
        <span>{{ currentAnnouncementIndex + 1 }}/{{ announcements.length }}</span>
        <i class="el-icon-arrow-right" @click="nextAnnouncement" />
      </div>
    </div>

    <div class="main-content">
      <div class="content-left">
        <div class="filter-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索话题..."
            prefix-icon="el-icon-search"
            clearable
            size="small"
            @clear="handleSearch"
            @keyup.enter.native="handleSearch"
          />
          <el-radio-group v-model="sortType" size="small" @change="handleSearch">
            <el-radio-button label="latest">最新</el-radio-button>
            <el-radio-button label="hot">热门</el-radio-button>
          </el-radio-group>
        </div>

        <div v-loading="loading" class="topic-list">
          <el-empty v-if="!loading && discussions.length === 0" description="暂无话题，快来发布第一个吧" />

          <div
            v-for="item in discussions"
            :key="item.id"
            class="topic-card"
            @click="showDiscussionDetail(item)"
          >
            <div class="card-header">
              <div class="author">
                <el-avatar :size="40" class="avatar">{{ getUserInitial(item.sender) }}</el-avatar>
                <div class="author-info">
                  <span class="name">{{ item.sender || '匿名用户' }}</span>
                  <span class="role-tag">{{ getRoleText(item.role) }}</span>
                </div>
              </div>
              <span class="time">{{ formatTime(item.createTime) }}</span>
            </div>

            <div class="card-body">
              <h3 class="title">{{ item.title }}</h3>
              <p class="excerpt">{{ getContentSummary(item.content) }}</p>
            </div>

            <div class="card-footer">
              <el-tag size="mini" :type="item.visibility === 1 ? 'success' : 'info'">
                {{ item.visibility === 1 ? '公开' : '班级可见' }}
              </el-tag>
              <div class="stats">
                <span class="stat-item" :class="{ active: item.liked }" @click.stop="handleLike(item)">
                  <i class="el-icon-thumb" />
                  {{ item.likeCount || 0 }}
                </span>
                <span class="stat-item">
                  <i class="el-icon-chat-dot-round" />
                  {{ item.commentCount || 0 }}
                </span>
                <span class="stat-item">
                  <i class="el-icon-view" />
                  {{ item.viewCount || 0 }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="total > 0" class="pagination">
          <el-pagination
            :current-page="currentPage"
            :page-sizes="[10, 20, 30]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>

      <div class="content-right">
        <div class="sidebar-card">
          <div class="card-title">
            <i class="el-icon-fire" />
            热门话题
          </div>
          <div class="hot-list">
            <div
              v-for="(topic, index) in hotTopics"
              :key="topic.id"
              class="hot-item"
              @click="showDiscussionDetail(topic)"
            >
              <span class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
              <span class="hot-title">{{ topic.title }}</span>
            </div>
            <div v-if="hotTopics.length === 0" class="empty-tip">暂无数据</div>
          </div>
        </div>

        <div class="sidebar-card stats-card">
          <div class="card-title">
            <i class="el-icon-data-line" />
            社区统计
          </div>
          <div class="stats-grid">
            <div class="stat-cell">
              <span class="val">{{ totalTopics }}</span>
              <span class="lbl">话题</span>
            </div>
            <div class="stat-cell">
              <span class="val">{{ totalComments }}</span>
              <span class="lbl">评论</span>
            </div>
            <div class="stat-cell">
              <span class="val">{{ totalLikes }}</span>
              <span class="lbl">点赞</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog title="发布话题" :visible.sync="createDialogVisible" width="600px" :close-on-click-modal="false">
      <el-form ref="discussionForm" :model="discussionForm" label-position="top">
        <el-form-item label="话题标题" prop="title" :rules="[{ required: true, message: '请输入话题标题', trigger: 'blur' }]">
          <el-input v-model="discussionForm.title" placeholder="请输入话题标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="话题内容" prop="content" :rules="[{ required: true, message: '请输入话题内容', trigger: 'blur' }]">
          <el-input v-model="discussionForm.content" type="textarea" :rows="6" placeholder="请输入话题内容" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="可见范围">
          <el-radio-group v-model="discussionForm.visibility" @change="handleVisibilityChange">
            <el-radio :label="1">公开（所有人可见）</el-radio>
            <el-radio :label="0">仅班级可见</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="discussionForm.visibility === 0" label="选择班级">
          <ClassSelect v-model="discussionForm.gradeId" :is-multiple="false" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreateSubmit">发布</el-button>
      </span>
    </el-dialog>

    <el-dialog title="公告详情" :visible.sync="announcementDetailVisible" width="600px">
      <div class="announce-detail">
        <h3>{{ currentAnnouncement.title }}</h3>
        <div class="meta">
          <span>发布人：{{ currentAnnouncement.realName || '系统管理员' }}</span>
          <span>时间：{{ formatDetailTime(currentAnnouncement.createTime) }}</span>
        </div>
        <el-divider />
        <div class="content" v-html="currentAnnouncement.content" />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { discussionpageStudent, discussionAdd, doLike, discussionDel } from '@/api/discussion'
import { noticeGetNew } from '@/api/notice'
import ClassSelect from '@/components/ClassSelect'

export default {
  name: 'DiscussionCenter',
  components: { ClassSelect },
  data() {
    return {
      loading: false,
      submitting: false,
      createDialogVisible: false,
      announcementDetailVisible: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      totalTopics: 0,
      totalComments: 0,
      totalLikes: 0,
      searchKeyword: '',
      sortType: 'latest',
      announcements: [],
      currentAnnouncementIndex: 0,
      currentAnnouncement: {},
      discussions: [],
      hotTopics: [],
      discussionForm: {
        title: '',
        content: '',
        gradeId: null,
        visibility: 1
      }
    }
  },
  created() {
    this.loadData()
    this.startAnnouncementCarousel()
  },
  beforeDestroy() {
    this.stopAnnouncementCarousel()
  },
  methods: {
    async loadData() {
      await Promise.all([
        this.getAnnouncements(),
        this.getDiscussions(),
        this.getHotTopics()
      ])
    },
    async getAnnouncements() {
      try {
        const res = await noticeGetNew({ pageNum: 1, pageSize: 5 })
        if (res.code) {
          this.announcements = res.data.records || []
        }
      } catch (e) { /* ignore */ }
    },
    async getDiscussions() {
      this.loading = true
      try {
        const params = {
          currentPage: this.currentPage,
          size: this.pageSize,
          title: this.searchKeyword || null
        }
        const res = await discussionpageStudent(params)
        if (res && res.code) {
          this.discussions = res.data.records || []
          this.total = res.data.total || 0
          this.calculateStats()
        }
      } catch (e) {
        this.$message.error('获取话题失败')
      } finally {
        this.loading = false
      }
    },
    calculateStats() {
      this.totalTopics = this.total
      this.totalComments = this.discussions.reduce((sum, d) => sum + (d.commentCount || 0), 0)
      this.totalLikes = this.discussions.reduce((sum, d) => sum + (d.likeCount || 0), 0)
    },
    async getHotTopics() {
      try {
        const res = await discussionpageStudent({ currentPage: 1, size: 10 })
        if (res && res.code) {
          this.hotTopics = (res.data.records || [])
            .sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
            .slice(0, 5)
        }
      } catch (e) { /* ignore */ }
    },
    startAnnouncementCarousel() {
      this.carouselTimer = setInterval(() => this.nextAnnouncement(), 5000)
    },
    stopAnnouncementCarousel() {
      if (this.carouselTimer) {
        clearInterval(this.carouselTimer)
      }
    },
    prevAnnouncement() {
      if (this.currentAnnouncementIndex > 0) this.currentAnnouncementIndex--
      else this.currentAnnouncementIndex = this.announcements.length - 1
    },
    nextAnnouncement() {
      if (this.currentAnnouncementIndex < this.announcements.length - 1) this.currentAnnouncementIndex++
      else this.currentAnnouncementIndex = 0
    },
    showAnnouncementDetail(announcement) {
      this.currentAnnouncement = announcement
      this.announcementDetailVisible = true
    },
    showDiscussionDetail(discussion) {
      this.$router.push({ name: 'discussion-detail', query: { discussionId: discussion.id } })
    },
    async handleLike(discussion) {
      try {
        const res = await doLike({ discussionId: discussion.id })
        if (res.code) {
          discussion.liked = !discussion.liked
          discussion.likeCount = discussion.liked ? (discussion.likeCount || 0) + 1 : Math.max(0, (discussion.likeCount || 1) - 1)
          this.$message.success(discussion.liked ? '点赞成功' : '取消点赞')
        }
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    handleCreateDiscussion() {
      this.createDialogVisible = true
      this.discussionForm = { title: '', content: '', gradeId: null, visibility: 1 }
      this.$nextTick(() => {
        if (this.$refs.discussionForm) this.$refs.discussionForm.clearValidate()
      })
    },
    handleVisibilityChange(value) {
      if (value === 1) this.discussionForm.gradeId = null
    },
    async handleCreateSubmit() {
      this.$refs.discussionForm.validate(async(valid) => {
        if (!valid) return
        if (this.discussionForm.visibility === 0 && !this.discussionForm.gradeId) {
          this.$message.warning('请选择可见班级')
          return
        }
        this.submitting = true
        try {
          const res = await discussionAdd(this.discussionForm)
          if (res.code) {
            this.$message.success('话题发布成功')
            this.createDialogVisible = false
            this.getDiscussions()
            this.getHotTopics()
          } else {
            this.$message.error(res.msg || '发布失败')
          }
        } catch (e) {
          this.$message.error('发布失败')
        } finally {
          this.submitting = false
        }
      })
    },
    handleSearch() {
      this.currentPage = 1
      this.getDiscussions()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.getDiscussions()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.getDiscussions()
    },
    formatTime(time) {
      if (!time) return ''
      const d = new Date(time)
      const now = new Date()
      const diff = now - d
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
      return `${d.getMonth() + 1}-${d.getDate()}`
    },
    formatDetailTime(time) {
      if (!time) return ''
      const d = new Date(time)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    },
    getContentSummary(content) {
      if (!content) return '暂无内容'
      const text = content.replace(/<[^>]*>/g, '')
      return text.length > 100 ? text.substring(0, 100) + '...' : text
    },
    getUserInitial(name) {
      if (!name) return '?'
      const c = name.charAt(0)
      return /[\u4e00-\u9fa5]/.test(c) ? c : c.toUpperCase()
    },
    getRoleText(role) {
      return { student: '学生', teacher: '教师', admin: '管理员' }[role] || '用户'
    }
  }
}
</script>

<style lang="scss" scoped>
.discussion-center {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: #fff;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 8px;
    display: flex;
    align-items: center;
    gap: 10px;

    i { font-size: 28px; }
  }

  .page-subtitle {
    margin: 0;
    opacity: 0.9;
    font-size: 14px;
  }

  .el-button {
    background: rgba(255, 255, 255, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.3);
    color: #fff;

    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

.announcement-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: #fff8e6;
  border: 1px solid #ffd666;
  border-radius: 8px;
  margin-bottom: 20px;
  color: #d48806;

  > i { font-size: 18px; }

  .announce-content {
    flex: 1;
    overflow: hidden;

    .announce-text {
      cursor: pointer;
      &:hover { text-decoration: underline; }
    }
  }

  .announce-controls {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;

    i {
      cursor: pointer;
      padding: 4px;
      &:hover { color: #fa8c16; }
    }
  }
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 20px;
}

.content-left {
  min-width: 0;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 16px;

  .el-input {
    width: 240px;
  }
}

.topic-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 300px;
}

.topic-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e8e8e8;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    border-color: #d9d9d9;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .author {
      display: flex;
      align-items: center;
      gap: 10px;

      .avatar {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }

      .author-info {
        display: flex;
        flex-direction: column;
        gap: 2px;

        .name {
          font-weight: 500;
          color: #333;
        }

        .role-tag {
          font-size: 11px;
          color: #999;
        }
      }
    }

    .time {
      font-size: 12px;
      color: #999;
    }
  }

  .card-body {
    margin-bottom: 12px;

    .title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      margin: 0 0 8px;
      line-height: 1.4;
    }

    .excerpt {
      font-size: 13px;
      color: #666;
      line-height: 1.6;
      margin: 0;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .stats {
      display: flex;
      gap: 16px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #999;
        transition: color 0.2s;

        &:hover, &.active {
          color: #1890ff;
        }

        &.active i {
          color: #f5222d;
        }
      }
    }
  }
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.content-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #e8e8e8;

  .card-title {
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
    padding-bottom: 10px;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    align-items: center;
    gap: 6px;

    i { color: #fa8c16; }
  }
}

.hot-list {
  .hot-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 8px 0;
    cursor: pointer;

    &:hover .hot-title {
      color: #1890ff;
    }

    .rank {
      width: 20px;
      height: 20px;
      border-radius: 4px;
      background: #f0f0f0;
      color: #999;
      font-size: 12px;
      font-weight: 600;
      display: flex;
      align-items: center;
      justify-content: center;

      &.top {
        background: linear-gradient(135deg, #fa8c16 0%, #fa541c 100%);
        color: #fff;
      }
    }

    .hot-title {
      flex: 1;
      font-size: 13px;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .empty-tip {
    text-align: center;
    color: #999;
    font-size: 12px;
    padding: 12px 0;
  }
}

.stats-card .stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;

  .stat-cell {
    text-align: center;
    padding: 10px 4px;
    background: #fafafa;
    border-radius: 8px;

    .val {
      display: block;
      font-size: 18px;
      font-weight: 600;
      color: #1890ff;
    }

    .lbl {
      font-size: 11px;
      color: #999;
    }
  }
}

.announce-detail {
  h3 {
    margin: 0 0 10px;
    color: #333;
  }

  .meta {
    font-size: 13px;
    color: #999;
    display: flex;
    gap: 20px;
  }

  .content {
    line-height: 1.8;
  }
}

.slide-enter-active, .slide-leave-active {
  transition: all 0.3s;
}
.slide-enter {
  opacity: 0;
  transform: translateX(20px);
}
.slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

@media (max-width: 992px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}
</style>
