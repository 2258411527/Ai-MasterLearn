<template>
  <div class="detail-page">
    <div class="back-bar">
      <el-button type="text" icon="el-icon-arrow-left" @click="$router.go(-1)">返回列表</el-button>
    </div>

    <div v-loading="loading" class="main-content">
      <div class="topic-section">
        <div class="topic-header">
          <div class="author-info">
            <el-avatar :size="48" class="avatar">{{ getUserInitial(discussion.sender) }}</el-avatar>
            <div class="info">
              <span class="name">{{ discussion.sender || '匿名用户' }}</span>
              <span class="role">{{ getRoleText(discussion.role) }}</span>
            </div>
          </div>
          <div class="topic-actions">
            <el-button type="text" :class="{ liked: discussion.liked }" @click="handleDiscussionLike">
              <i class="el-icon-thumb" />
              {{ discussion.likeCount || 0 }}
            </el-button>
            <el-button type="text" @click="handleShare">
              <i class="el-icon-share" />
              分享
            </el-button>
            <el-button v-if="canDelete" type="text" class="delete-btn" @click="handleDelete">
              <i class="el-icon-delete" />
              删除
            </el-button>
          </div>
        </div>

        <div class="topic-body">
          <h1 class="title">{{ discussion.title }}</h1>
          <div class="content" v-html="discussion.content" />
        </div>

        <div class="topic-footer">
          <div class="stats">
            <span><i class="el-icon-view" /> {{ discussion.viewCount || 0 }} 浏览</span>
            <span><i class="el-icon-chat-dot-round" /> {{ discussion.commentCount || 0 }} 评论</span>
            <span><i class="el-icon-thumb" /> {{ discussion.likeCount || 0 }} 点赞</span>
          </div>
          <div class="meta">
            <el-tag size="small" :type="discussion.visibility === 1 ? 'success' : 'info'">
              {{ discussion.visibility === 1 ? '公开' : '班级可见' }}
            </el-tag>
            <span class="time">{{ formatTime(discussion.createTime) }}</span>
          </div>
        </div>
      </div>

      <div class="comments-section">
        <div class="section-header">
          <h3>评论 ({{ comments.length }})</h3>
          <el-radio-group v-model="sortBy" size="mini" @change="handleSortChange">
            <el-radio-button label="time">最新</el-radio-button>
            <el-radio-button label="like">最热</el-radio-button>
          </el-radio-group>
        </div>

        <div class="comment-input">
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            placeholder="发表你的看法..."
            maxlength="500"
            show-word-limit
          />
          <div class="input-actions">
            <el-button type="primary" size="small" :loading="submittingComment" :disabled="!commentContent.trim()" @click="submitComment">
              发表评论
            </el-button>
          </div>
        </div>

        <div v-loading="commentsLoading" class="comment-list">
          <el-empty v-if="!commentsLoading && comments.length === 0" description="暂无评论，快来抢沙发" />

          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <div class="comment-author">
                <el-avatar :size="36" class="avatar-sm">{{ getUserInitial(comment.realName) }}</el-avatar>
                <div class="author-info-sm">
                  <span class="name">{{ comment.realName || '匿名' }}</span>
                  <span class="role">{{ getRoleText(comment.role) }}</span>
                </div>
              </div>
              <span class="time">{{ formatTime(comment.createTime) }}</span>
            </div>

            <div class="comment-content">{{ comment.content }}</div>

            <div class="comment-actions">
              <span :class="{ liked: comment.liked }" @click="handleCommentLike(comment)">
                <i class="el-icon-thumb" /> {{ comment.likeCount || 0 }}
              </span>
              <span @click="toggleReply(comment)">
                <i class="el-icon-chat-round" /> 回复
              </span>
              <span v-if="canDeleteComment(comment)" class="delete" @click="handleDeleteComment(comment)">
                <i class="el-icon-delete" /> 删除
              </span>
            </div>

            <div v-if="comment.childReplies && comment.childReplies.length > 0" class="replies">
              <div v-for="reply in comment.childReplies" :key="reply.id" class="reply-item">
                <div class="reply-header">
                  <el-avatar :size="28" class="avatar-xs">{{ getUserInitial(reply.realName) }}</el-avatar>
                  <span class="name">{{ reply.realName }}</span>
                  <span class="time">{{ formatTime(reply.createTime) }}</span>
                </div>
                <div class="reply-content">{{ reply.content }}</div>
                <div class="reply-actions">
                  <span :class="{ liked: reply.liked }" @click="handleReplyLike(comment, reply)">
                    <i class="el-icon-thumb" /> {{ reply.likeCount || 0 }}
                  </span>
                  <span v-if="canDeleteComment(reply)" class="delete" @click="handleDeleteComment(reply)">
                    <i class="el-icon-delete" />
                  </span>
                </div>
              </div>
            </div>

            <div v-if="comment.showReply" class="reply-input">
              <el-input v-model="comment.replyContent" type="textarea" :rows="2" placeholder="回复..." maxlength="300" />
              <div class="reply-actions-bar">
                <el-button size="mini" @click="comment.showReply = false">取消</el-button>
                <el-button type="primary" size="mini" :loading="comment.replying" :disabled="!comment.replyContent.trim()" @click="submitReply(comment)">
                  回复
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { discussionDetail, doLike, discussionDel } from '@/api/discussion'
import { replyAdd, replyDel, replyquery } from '@/api/reply'
import { getUserId, getRole } from '@/utils/auth'

export default {
  name: 'DiscussionDetail',
  data() {
    return {
      loading: false,
      commentsLoading: false,
      submittingComment: false,
      discussion: {},
      comments: [],
      commentContent: '',
      sortBy: 'time',
      currentUserId: getUserId(),
      currentRole: getRole()
    }
  },
  computed: {
    discussionId() {
      return this.$route.query.discussionId
    },
    canDelete() {
      if (this.currentRole === '3' || this.currentRole === '2') return true
      return this.discussion.userId === this.currentUserId
    }
  },
  created() {
    if (this.discussionId) {
      this.loadDiscussionDetail()
      this.loadComments()
    } else {
      this.$message.error('话题ID不存在')
      this.$router.go(-1)
    }
  },
  methods: {
    async loadDiscussionDetail() {
      this.loading = true
      try {
        const res = await discussionDetail(this.discussionId)
        if (res.code) {
          this.discussion = res.data || {}
        } else {
          this.$message.error(res.msg || '获取话题详情失败')
          this.$router.go(-1)
        }
      } catch (e) {
        this.$message.error('获取话题详情失败')
        this.$router.go(-1)
      } finally {
        this.loading = false
      }
    },
    async loadComments() {
      this.commentsLoading = true
      try {
        const res = await replyquery(this.sortBy === 'time' ? 2 : 4, this.discussionId)
        if (res.code) {
          this.comments = (res.data || []).map(c => ({
            ...c,
            showReply: false,
            replyContent: '',
            replying: false
          }))
        }
      } catch (e) {
        this.$message.error('获取评论失败')
      } finally {
        this.commentsLoading = false
      }
    },
    handleSortChange() {
      this.loadComments()
    },
    async handleDiscussionLike() {
      try {
        const res = await doLike({ discussionId: this.discussionId })
        if (res.code) {
          this.discussion.liked = !this.discussion.liked
          this.discussion.likeCount = this.discussion.liked
            ? (this.discussion.likeCount || 0) + 1
            : Math.max(0, (this.discussion.likeCount || 1) - 1)
          this.$message.success(this.discussion.liked ? '点赞成功' : '取消点赞')
        }
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    async handleCommentLike(comment) {
      try {
        const res = await doLike({ discussionId: this.discussionId, replyId: comment.id })
        if (res.code) {
          comment.liked = !comment.liked
          comment.likeCount = comment.liked ? (comment.likeCount || 0) + 1 : Math.max(0, (comment.likeCount || 1) - 1)
          this.$message.success(comment.liked ? '点赞成功' : '取消点赞')
        }
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    async handleReplyLike(parent, reply) {
      try {
        const res = await doLike({ discussionId: this.discussionId, replyId: reply.id })
        if (res.code) {
          reply.liked = !reply.liked
          reply.likeCount = reply.liked ? (reply.likeCount || 0) + 1 : Math.max(0, (reply.likeCount || 1) - 1)
          this.$message.success(reply.liked ? '点赞成功' : '取消点赞')
        }
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    async submitComment() {
      if (!this.commentContent.trim()) return
      this.submittingComment = true
      try {
        const res = await replyAdd({ discussionId: this.discussionId, content: this.commentContent.trim() })
        if (res.code) {
          this.$message.success('评论成功')
          this.commentContent = ''
          this.loadComments()
          this.discussion.commentCount = (this.discussion.commentCount || 0) + 1
        } else {
          this.$message.error(res.msg || '评论失败')
        }
      } catch (e) {
        this.$message.error('评论失败')
      } finally {
        this.submittingComment = false
      }
    },
    toggleReply(comment) {
      this.$set(comment, 'showReply', !comment.showReply)
      if (!comment.showReply) comment.replyContent = ''
    },
    async submitReply(comment) {
      if (!comment.replyContent.trim()) return
      this.$set(comment, 'replying', true)
      try {
        const res = await replyAdd({ discussionId: this.discussionId, content: comment.replyContent.trim(), parentId: comment.id })
        if (res.code) {
          this.$message.success('回复成功')
          comment.showReply = false
          comment.replyContent = ''
          this.loadComments()
        } else {
          this.$message.error(res.msg || '回复失败')
        }
      } catch (e) {
        this.$message.error('回复失败')
      } finally {
        comment.replying = false
      }
    },
    handleDelete() {
      this.$confirm('确定要删除这个话题吗？删除后无法恢复', '提示', { type: 'warning' }).then(async() => {
        try {
          const res = await discussionDel(this.discussionId)
          if (res.code) {
            this.$message.success('删除成功')
            this.$router.go(-1)
          } else {
            this.$message.error(res.msg || '删除失败')
          }
        } catch (e) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    handleDeleteComment(comment) {
      this.$confirm('确定要删除这条评论吗？', '提示', { type: 'warning' }).then(async() => {
        try {
          const res = await replyDel(comment.id)
          if (res.code) {
            this.$message.success('删除成功')
            this.loadComments()
            this.discussion.commentCount = Math.max(0, (this.discussion.commentCount || 1) - 1)
          } else {
            this.$message.error(res.msg || '删除失败')
          }
        } catch (e) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },
    canDeleteComment(comment) {
      if (this.currentRole === '3' || this.currentRole === '2') return true
      return comment.userId === this.currentUserId
    },
    handleShare() {
      const url = window.location.href
      navigator.clipboard.writeText(url).then(() => {
        this.$message.success('链接已复制')
      }).catch(() => {
        const ta = document.createElement('textarea')
        ta.value = url
        document.body.appendChild(ta)
        ta.select()
        document.execCommand('copy')
        document.body.removeChild(ta)
        this.$message.success('链接已复制')
      })
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
.detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.back-bar {
  margin-bottom: 16px;
  .el-button { color: #666; &:hover { color: #333; } }
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.topic-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e8e8e8;

  .topic-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .author-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .avatar {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }

      .info {
        display: flex;
        flex-direction: column;
        gap: 2px;
        .name { font-weight: 600; color: #333; }
        .role { font-size: 12px; color: #999; }
      }
    }

    .topic-actions {
      display: flex;
      gap: 8px;

      .el-button {
        color: #666;
        &:hover { color: #333; }
        &.liked { color: #f5222d; }
        &.delete-btn:hover { color: #f5222d; }
      }
    }
  }

  .topic-body {
    .title {
      font-size: 22px;
      font-weight: 600;
      color: #333;
      margin: 0 0 16px;
      line-height: 1.4;
    }

    .content {
      font-size: 15px;
      line-height: 1.8;
      color: #444;
    }
  }

  .topic-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;

    .stats {
      display: flex;
      gap: 20px;
      font-size: 13px;
      color: #999;

      i { margin-right: 4px; }
    }

    .meta {
      display: flex;
      align-items: center;
      gap: 12px;

      .time {
        font-size: 13px;
        color: #999;
      }
    }
  }
}

.comments-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e8e8e8;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      margin: 0;
      font-size: 16px;
      color: #333;
    }
  }

  .comment-input {
    margin-bottom: 24px;

    .input-actions {
      margin-top: 12px;
      text-align: right;
    }
  }

  .comment-list {
    min-height: 100px;
  }

  .comment-item {
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child { border-bottom: none; }

    .comment-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;

      .comment-author {
        display: flex;
        align-items: center;
        gap: 10px;

        .avatar-sm {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: #fff;
        }

        .author-info-sm {
          display: flex;
          flex-direction: column;
          gap: 2px;
          .name { font-weight: 500; font-size: 14px; color: #333; }
          .role { font-size: 11px; color: #999; }
        }
      }

      .time { font-size: 12px; color: #999; }
    }

    .comment-content {
      font-size: 14px;
      line-height: 1.7;
      color: #444;
      padding: 10px 14px;
      background: #fafafa;
      border-radius: 8px;
      margin-bottom: 10px;
    }

    .comment-actions {
      display: flex;
      gap: 16px;
      font-size: 13px;
      color: #999;

      > span {
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 4px;
        transition: color 0.2s;

        &:hover { color: #1890ff; }
        &.liked { color: #f5222d; }
        &.delete:hover { color: #f5222d; }
      }
    }

    .replies {
      margin-top: 16px;
      padding-left: 20px;
      border-left: 2px solid #e8e8e8;

      .reply-item {
        padding: 12px;
        margin-bottom: 12px;
        background: #fafafa;
        border-radius: 8px;

        &:last-child { margin-bottom: 0; }

        .reply-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;

          .avatar-xs {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
          }

          .name { font-size: 13px; font-weight: 500; color: #333; }
          .time { font-size: 11px; color: #999; }
        }

        .reply-content {
          font-size: 13px;
          line-height: 1.6;
          color: #444;
        }

        .reply-actions {
          display: flex;
          gap: 12px;
          margin-top: 8px;
          font-size: 12px;
          color: #999;

          > span {
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 4px;

            &:hover { color: #1890ff; }
            &.liked { color: #f5222d; }
            &.delete:hover { color: #f5222d; }
          }
        }
      }
    }

    .reply-input {
      margin-top: 12px;
      padding: 12px;
      background: #fafafa;
      border-radius: 8px;

      .reply-actions-bar {
        margin-top: 10px;
        text-align: right;
      }
    }
  }
}
</style>
