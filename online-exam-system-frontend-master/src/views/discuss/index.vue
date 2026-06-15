<template>
  <div class="forum-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
    </div>

    <div class="content-wrapper">
      <!-- 顶部标题栏 -->
      <div class="forum-header glass-card">
        <div class="header-content">
          <div class="header-left">
            <h1 class="forum-title">
              <i class="el-icon-chat-dot-square"></i>
              交流中心
            </h1>
            <p class="forum-subtitle">与志同道合的伙伴一起探讨学习</p>
          </div>
          <div class="header-actions">
            <el-input
              v-model="searchForm.searchTitle"
              placeholder="搜索话题..."
              class="search-input"
              prefix-icon="el-icon-search"
              clearable
              @clear="handleSearch"
              @keyup.enter.native="handleSearch"
            />
            <el-button
              type="primary"
              icon="el-icon-edit"
              class="new-topic-btn"
              @click="handleCreateDiscussion"
            >
              发布话题
            </el-button>
          </div>
        </div>
      </div>

      <!-- 公告栏 -->
      <div v-if="noticeList.length > 0" class="notice-board glass-card">
        <div class="notice-header">
          <div class="notice-title-section">
            <i class="el-icon-magic-stick"></i>
            <span class="notice-title">最新公告</span>
          </div>
          <div class="notice-controls">
            <el-button
              type="text"
              size="mini"
              icon="el-icon-arrow-left"
              :disabled="currentNoticeIndex === 0"
              @click="scrollLeft"
            />
            <span class="notice-counter">{{ currentNoticeIndex + 1 }}/{{ noticeList.length }}</span>
            <el-button
              type="text"
              size="mini"
              icon="el-icon-arrow-right"
              :disabled="currentNoticeIndex === noticeList.length - 1"
              @click="scrollRight"
            />
          </div>
        </div>
        <div class="notice-list">
          <div
            v-for="(notice, index) in noticeList"
            v-show="index === currentNoticeIndex"
            :key="index"
            class="notice-item"
            :class="{ 'active': index === currentNoticeIndex }"
            @click="showNoticeDetail(notice)"
          >
            <div class="notice-badge">{{ index + 1 }}</div>
            <div class="notice-content">
              <span class="notice-text">{{ notice.title }}</span>
              <span class="notice-time">{{ formatTime(notice.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 话题列表 -->
      <div v-loading="loading" class="topic-list">
        <el-empty v-if="!loading && data.records && data.records.length === 0" description="暂无话题" />

        <div
          v-for="discussion in data.records"
          :key="discussion.id"
          class="topic-card glass-card"
        >
          <div class="topic-main" @click="showRow(discussion)">
            <!-- 用户信息 -->
            <div class="topic-author">
              <div class="author-avatar">
                <div class="avatar-placeholder">{{ getAvatarInitial(discussion.sender) }}</div>
              </div>
              <div class="author-info">
                <div class="author-name">{{ discussion.sender }}</div>
                <el-tag size="mini" :type="getRoleTagType(discussion.role)">
                  {{ getRoleText(discussion.role) }}
                </el-tag>
              </div>
            </div>

            <!-- 话题内容 -->
            <div class="topic-content">
              <h3 class="topic-title">{{ discussion.title }}</h3>

              <!-- 话题标签 -->
              <div class="topic-tags">
                <el-tag
                  :type="discussion.visibility === 1 ? 'success' : 'info'"
                  size="small"
                >
                  {{ discussion.visibility === 1 ? '公开' : '班级' }}
                </el-tag>
              </div>
            </div>
          </div>

          <!-- 底部统计和操作 -->
          <div class="topic-footer">
            <div class="topic-stats">
              <div class="stat-item">
                <i class="el-icon-chat-dot-round"></i>
                <span>{{ discussion.commentCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <i class="el-icon-thumb"></i>
                <span>{{ discussion.likeCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <i class="el-icon-view"></i>
                <span>{{ discussion.viewCount || 0 }}</span>
              </div>
              <div class="stat-time">
                {{ formatDetailTime(discussion.createTime) }}
              </div>
            </div>

            <div class="topic-actions">
              <el-button
                type="primary"
                size="small"
                icon="el-icon-view"
                @click.stop="showRow(discussion)"
              >
                查看详情
              </el-button>
              <el-button
                v-if="canDeleteDiscussion(discussion)"
                type="danger"
                size="small"
                icon="el-icon-delete"
                @click.stop="handleDel(discussion.id)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="data.total > 0" class="pagination-container glass-card">
        <el-pagination
          :current-page="data.current"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="data.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="data.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 发布讨论对话框 -->
    <el-dialog title="发布话题" :visible.sync="dialogVisible" width="600px" top="10vh">
      <el-form :model="discussionForm" label-position="top">
        <el-form-item label="标题">
          <el-input
            v-model="discussionForm.title"
            placeholder="请输入标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="内容">
          <el-input
            v-model="discussionForm.content"
            type="textarea"
            :rows="8"
            placeholder="请输入内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="可见范围">
          <el-radio-group v-model="discussionForm.visibility" @change="handleVisibilityChange">
            <el-radio :label="1">公开（所有人可见）</el-radio>
            <el-radio :label="0">指定班级</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="discussionForm.visibility === 0" label="选择班级">
          <ClassSelect v-model="discussionForm.gradeId" :is-multiple="false" />
        </el-form-item>
      </el-form>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleConfirm">确定</el-button>
      </span>
    </el-dialog>

    <!-- 公告详情弹窗 -->
    <el-dialog
      title="公告详情"
      :visible.sync="noticeDialogVisible"
      width="700px"
      top="10vh"
    >
      <div class="notice-detail-content">
        <div class="notice-detail-header">
          <h3>{{ currentNotice.title }}</h3>
          <div class="notice-detail-meta">
            <span class="notice-author">发布人：{{ currentNotice.realName || '系统管理员' }}</span>
            <span class="notice-time">发布时间：{{ formatDetailTime(currentNotice.createTime) }}</span>
          </div>
        </div>
        <div class="notice-detail-body">
          <div class="notice-content" v-html="currentNotice.content" />
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="noticeDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { discussionpageOwner, discussionpageStudent, discussionAdd, discussionDel } from '@/api/discussion'
import { noticeGetNew } from '@/api/notice'
import ClassSelect from '@/components/ClassSelect'
import { getRole } from '@/utils/auth'

export default {
  components: {
    ClassSelect
  },
  data() {
    return {
      loading: false,
      submitting: false,
      dialogVisible: false,
      currentRole: null,
      pageNum: 1,
      pageSize: 10,
      data: { records: [], total: 0, current: 1, size: 10 },
      searchForm: {
        searchTitle: ''
      },
      discussionForm: {
        title: null,
        content: null,
        gradeId: null,
        visibility: 1
      },
      defaultAvatar: require('@/assets/default-avatar.png'),
      noticeList: [],
      currentNoticeIndex: 0,
      noticeDialogVisible: false,
      currentNotice: {}
    }
  },
  created() {
    this.initRole()
    this.loadData()
  },
  methods: {
    initRole() {
      const roleId = getRole()
      const roleMap = {
        1: 'student',
        2: 'teacher',
        3: 'admin'
      }
      this.currentRole = roleMap[roleId] || 'student'
    },

    async loadData() {
      await Promise.all([
        this.getDiscussionPage(),
        this.getNoticeList()
      ])
    },

    async getNoticeList() {
      try {
        const res = await noticeGetNew({ pageNum: 1, pageSize: 5 })
        if (res.code) {
          this.noticeList = res.data.records || []
          this.currentNoticeIndex = 0
        }
      } catch (error) {
        console.error('获取公告失败:', error)
      }
    },

    scrollLeft() {
      if (this.currentNoticeIndex > 0) {
        this.currentNoticeIndex--
      }
    },

    scrollRight() {
      if (this.currentNoticeIndex < this.noticeList.length - 1) {
        this.currentNoticeIndex++
      }
    },

    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      return `${date.getMonth() + 1}-${date.getDate()}`
    },

    showNoticeDetail(notice) {
      this.currentNotice = notice
      this.noticeDialogVisible = true
    },

    formatDetailTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}`
    },

    handleCreateDiscussion() {
      this.dialogVisible = true
      this.resetForm()
    },

    resetForm() {
      this.discussionForm = {
        title: null,
        content: null,
        gradeId: null,
        visibility: 1
      }
    },

    handleVisibilityChange(value) {
      if (value === 1) {
        this.discussionForm.gradeId = null
      }
    },

    async handleConfirm() {
      if (!this.discussionForm.title || !this.discussionForm.content) {
        this.$message.warning('请填写标题和内容')
        return
      }

      if (this.discussionForm.visibility === 0 && !this.discussionForm.gradeId) {
        this.$message.warning('请选择可见班级')
        return
      }

      this.submitting = true
      try {
        const res = await discussionAdd(this.discussionForm)
        if (res.code) {
          this.$message.success(res.msg)
          this.dialogVisible = false
          this.resetForm()
          this.getDiscussionPage()
        } else {
          this.$message.error(res.msg)
        }
      } catch (error) {
        this.$message.error('发布失败，请重试')
      } finally {
        this.submitting = false
      }
    },

    handleDel(id) {
      this.$confirm('此操作将永久删除该话题，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.delDiscussion(id)
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },

    async delDiscussion(id) {
      try {
        const res = await discussionDel(id)
        if (res.code) {
          this.$message.success(res.msg)
          this.getDiscussionPage()
        } else {
          this.$message.error(res.msg)
        }
      } catch (error) {
        this.$message.error('删除失败，请重试')
      }
    },

    showRow(row) {
      this.$router.push({
        name: 'discussion-detail',
        query: { discussionId: row.id }
      })
    },

    handleSearch() {
      this.pageNum = 1
      this.getDiscussionPage()
    },

    async getDiscussionPage(pageNum = this.pageNum, pageSize = this.pageSize) {
      this.loading = true
      const params = {
        currentPage: pageNum,
        size: pageSize,
        title: this.searchForm.searchTitle || null
      }

      try {
        let res
        res = await discussionpageStudent(params)

        if (res && res.code) {
          this.data = res.data
        } else {
          this.$message.error(res?.msg || '获取话题失败')
          this.data = { records: [], total: 0, current: 1, size: 10 }
        }
      } catch (error) {
        console.error('获取话题异常:', error)
        this.$message.error('获取话题失败，请检查网络连接')
        this.data = { records: [], total: 0, current: 1, size: 10 }
      } finally {
        this.loading = false
      }
    },

    handleSizeChange(val) {
      this.pageSize = val
      this.pageNum = 1
      this.getDiscussionPage(1, val)
    },

    handleCurrentChange(val) {
      this.pageNum = val
      this.getDiscussionPage(val, this.pageSize)
    },

    canDeleteDiscussion(row) {
      if (this.currentRole === 'teacher' || this.currentRole === 'admin') {
        return true
      }
      return this.currentRole === 'student'
    },

    getRoleText(role) {
      const roleMap = {
        'student': '学生',
        'teacher': '教师',
        'admin': '管理员'
      }
      return roleMap[role] || '用户'
    },

    getRoleTagType(role) {
      const typeMap = {
        'student': '',
        'teacher': 'warning',
        'admin': 'danger'
      }
      return typeMap[role] || ''
    },

    getAvatarInitial(name) {
      if (!name) return 'U'
      const firstChar = name.charAt(0)
      if (/[\u4e00-\u9fa5]/.test(firstChar)) {
        return firstChar
      }
      return firstChar.toUpperCase()
    }
  }
}
</script>

<style lang="scss" scoped>
.forum-container {
  min-height: 100vh;
  background: var(--bg-primary);
  position: relative;
  overflow: hidden;
  transition: background 0.3s ease;
}

.bg-decoration {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);

  &.orb-1 {
    width: 600px;
    height: 600px;
    background: linear-gradient(135deg, #06B6D4 0%, #6366F1 100%);
    top: -200px;
    left: -100px;
  }

  &.orb-2 {
    width: 400px;
    height: 400px;
    background: linear-gradient(135deg, #8B5CF6 0%, #EC4899 100%);
    bottom: -100px;
    right: -100px;
  }

  &.orb-3 {
    width: 300px;
    height: 300px;
    background: linear-gradient(135deg, #10B981 0%, #06B6D4 100%);
    top: 50%;
    right: 20%;
    transform: translateY(-50%);
  }
}

.content-wrapper {
  position: relative;
  z-index: 1;
  padding: 32px;
  max-width: 1200px;
  margin: 0 auto;
}

.glass-card {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  transition: all 0.3s ease;

  &:hover {
    border-color: var(--border-focus);
    transform: translateY(-2px);
  }
}

.forum-header {
  padding: 24px 32px;
  margin-bottom: 24px;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    @media (max-width: 768px) {
      flex-direction: column;
      gap: 16px;
    }
  }

  .header-left {
    .forum-title {
      margin: 0 0 8px 0;
      font-size: 28px;
      font-weight: 700;
      color: var(--text-primary);
      display: flex;
      align-items: center;
      gap: 12px;

      i {
        color: var(--accent-light);
        font-size: 32px;
      }
    }

    .forum-subtitle {
      margin: 0;
      font-size: 14px;
      color: var(--text-secondary);
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 16px;

    @media (max-width: 768px) {
      width: 100%;
      flex-direction: column;
    }

    .search-input {
      width: 300px;

      @media (max-width: 768px) { width: 100%; }

      ::v-deep .el-input__inner {
        background: var(--bg-input);
        border: 1px solid var(--border-primary);
        color: var(--text-primary);
        border-radius: 12px;
        padding: 10px 16px;

        &::placeholder { color: var(--text-hint); }

        &:focus {
          border-color: var(--accent);
          background: var(--bg-hover);
        }
      }
    }

    .new-topic-btn {
      border-radius: 12px;
      padding: 10px 24px;
      font-weight: 500;
      background: var(--accent-gradient);
      border: none;
      color: white;
      transition: all 0.2s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
      }
    }
  }
}

.notice-board {
  padding: 20px 24px;
  margin-bottom: 24px;

  .notice-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;

    .notice-title-section {
      display: flex;
      align-items: center;
      gap: 8px;

      i { color: var(--accent-light); font-size: 18px; }

      .notice-title {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
      }
    }

    .notice-controls {
      display: flex;
      align-items: center;
      gap: 8px;

      .notice-counter {
        font-size: 13px;
        color: var(--text-secondary);
        min-width: 45px;
        text-align: center;
      }
    }
  }

  .notice-item {
    display: flex;
    align-items: center;
    padding: 16px 20px;
    background: var(--bg-active);
    border: 1px solid var(--border-primary);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background: var(--sidebar-hover);
      border-color: var(--border-focus);
    }

    .notice-badge {
      width: 28px;
      height: 28px;
      background: var(--accent-gradient);
      color: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 13px;
      font-weight: 600;
      margin-right: 16px;
      flex-shrink: 0;
    }

    .notice-content {
      flex: 1;
      display: flex;
      justify-content: space-between;
      align-items: center;
      min-width: 0;

      .notice-text {
        font-size: 15px;
        color: var(--text-primary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
        margin-right: 16px;
      }

      .notice-time {
        font-size: 13px;
        color: var(--text-secondary);
        flex-shrink: 0;
      }
    }
  }
}

.topic-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 300px;
}

.topic-card {
  padding: 24px;

  .topic-main {
    display: flex;
    gap: 20px;
    cursor: pointer;
    margin-bottom: 20px;

    @media (max-width: 768px) { flex-direction: column; }
  }

  .topic-author {
    display: flex;
    align-items: center;
    gap: 14px;
    flex-shrink: 0;

    .author-avatar {
      width: 52px;
      height: 52px;
      border-radius: 50%;
      overflow: hidden;
      background: var(--accent-gradient);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      .avatar-placeholder {
        font-size: 22px;
        color: white;
        font-weight: 600;
      }
    }

    .author-info {
      .author-name {
        font-size: 15px;
        font-weight: 600;
        color: var(--text-primary);
        margin-bottom: 6px;
      }
    }
  }

  .topic-content {
    flex: 1;
    min-width: 0;

    .topic-title {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 12px 0;
      line-height: 1.6;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .topic-tags { display: flex; gap: 10px; }
  }

  .topic-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 20px;
    border-top: 1px solid var(--glass-border);

    @media (max-width: 768px) {
      flex-direction: column;
      gap: 16px;
      align-items: flex-start;
    }

    .topic-stats {
      display: flex;
      align-items: center;
      gap: 24px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 6px;
        color: var(--text-secondary);
        font-size: 14px;
        transition: color 0.2s ease;

        i { font-size: 16px; }

        &:hover { color: var(--accent-light); }
      }

      .stat-time {
        font-size: 13px;
        color: var(--text-hint);
        margin-left: 8px;
      }
    }

    .topic-actions {
      display: flex;
      gap: 12px;

      .el-button {
        border-radius: 8px;
        padding: 8px 16px;
        font-size: 13px;
        transition: all 0.2s ease;
      }
    }
  }
}

.pagination-container {
  margin-top: 24px;
  padding: 16px 24px;
  display: flex;
  justify-content: center;
}

.notice-detail-content {
  max-height: 500px;
  overflow-y: auto;
}

.notice-detail-header {
  border-bottom: 1px solid var(--glass-border);
  padding-bottom: 16px;
  margin-bottom: 16px;

  h3 {
    margin: 0 0 12px 0;
    color: var(--text-primary);
    font-size: 20px;
    font-weight: 600;
  }
}

.notice-detail-meta {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: var(--text-secondary);

  .notice-author {
    color: var(--accent-light);
    font-weight: 500;
  }
}

.notice-detail-body {
  line-height: 1.8;

  .notice-content {
    font-size: 15px;
    color: var(--text-secondary);

    p { margin-bottom: 12px; }

    img {
      max-width: 100%;
      height: auto;
      border-radius: 8px;
    }
  }
}

::v-deep .el-dialog {
  background: var(--dialog-bg);
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--dialog-border);
  border-radius: 16px;

  .el-dialog__title { color: var(--text-primary); font-weight: 600; }
  .el-form-item__label { color: var(--text-secondary); font-weight: 500; }

  .el-input__inner {
    background: var(--bg-input);
    border-color: var(--border-primary);
    color: var(--text-primary);
    border-radius: 10px;

    &::placeholder { color: var(--text-hint); }
    &:focus { border-color: var(--accent); }
  }

  .el-textarea__inner {
    background: var(--bg-input);
    border-color: var(--border-primary);
    color: var(--text-primary);
    border-radius: 10px;

    &::placeholder { color: var(--text-hint); }
    &:focus { border-color: var(--accent); }
  }

  .el-radio__label { color: var(--text-secondary); }
  .el-radio__input.is-checked .el-radio__inner { background: var(--accent); border-color: var(--accent); }
  .el-radio__input.is-checked + .el-radio__label { color: var(--accent-light); }
}

::v-deep .el-pagination {
  .el-pager li {
    background: var(--pagination-bg);
    border: 1px solid var(--border-primary);
    color: var(--pagination-text);
    border-radius: 8px;

    &.is-active {
      background: var(--accent-gradient);
      border-color: var(--accent);
      color: white;
    }

    &:hover { color: var(--accent-light); }
  }

  .btn-prev,
  .btn-next {
    background: var(--pagination-bg);
    border: 1px solid var(--border-primary);
    color: var(--pagination-text);
    border-radius: 8px;

    &:hover { color: var(--accent-light); }
  }
}

::v-deep .el-tag { border: none; border-radius: 6px; }

::v-deep .el-empty {
  .el-empty__description { color: var(--text-secondary); }
  .el-empty__image svg { fill: var(--text-hint); }
}

::v-deep .el-loading-mask { background-color: var(--bg-overlay); }
</style>
