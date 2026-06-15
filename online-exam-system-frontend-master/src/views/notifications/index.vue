<template>
  <div class="message-center">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">消息中心</h2>
        <p class="page-subtitle" v-if="unreadSummary.total > 0">{{ unreadSummary.total }} 条未读</p>
      </div>
      <el-button type="text" size="small" @click="handleMarkAllRead" :disabled="unreadSummary.total === 0">
        <i class="el-icon-check"></i> 全部已读
      </el-button>
    </div>

    <div class="filter-tabs">
      <el-radio-group v-model="activeTab" size="small" @change="handleTabChange">
        <el-radio-button label="all">
          全部
          <span v-if="unreadSummary.total > 0" class="tab-count">{{ unreadSummary.total }}</span>
        </el-radio-button>
        <el-radio-button label="notice">
          公告
          <span v-if="unreadSummary.notice > 0" class="tab-count">{{ unreadSummary.notice }}</span>
        </el-radio-button>
        <el-radio-button label="friend_message">
          好友消息
          <span v-if="unreadSummary.friend_message > 0" class="tab-count">{{ unreadSummary.friend_message }}</span>
        </el-radio-button>
        <el-radio-button label="system">
          系统
          <span v-if="systemUnread > 0" class="tab-count">{{ systemUnread }}</span>
        </el-radio-button>
      </el-radio-group>
    </div>

    <div v-if="mustReadCount > 0" class="must-read-banner">
      <i class="el-icon-warning" />
      <span>您有 <strong>{{ mustReadCount }}</strong> 条必读消息未查看</span>
    </div>

    <div v-loading="loading" class="message-list">
      <el-empty v-if="!loading && messages.length === 0" description="暂无消息" />

      <div
        v-for="item in messages"
        :key="item.type + '-' + item.id"
        class="message-item"
        :class="{ unread: item.isRead === 0, 'must-read': item.isMustRead === 1 }"
        @click="handleItemClick(item)"
      >
        <div class="item-icon" :class="getIconClass(item.type)">
          <i :class="getIcon(item.type)" />
        </div>
        <div class="item-body">
          <div class="item-top">
            <span class="item-title">{{ item.title }}</span>
            <span class="item-time">{{ formatTime(item.createTime) }}</span>
          </div>
          <div class="item-excerpt">{{ getExcerpt(item.content) }}</div>
          <div class="item-bottom">
            <el-tag :type="getTypeTag(item.type)" size="mini" effect="plain">{{ getTypeLabel(item.type) }}</el-tag>
            <el-tag v-if="item.isMustRead === 1" type="danger" size="mini" effect="dark">必读</el-tag>
            <span v-if="item.senderName" class="item-sender">{{ item.senderName }}</span>
          </div>
        </div>
        <div v-if="item.isRead === 0" class="unread-dot"></div>
      </div>
    </div>

    <div v-if="total > pageSize" class="pagination">
      <el-pagination
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <el-dialog
      :visible.sync="detailVisible"
      :title="currentItem.title"
      width="600px"
      custom-class="message-detail-dialog"
    >
      <div class="detail-meta">
        <el-tag :type="getTypeTag(currentItem.type)" size="mini">{{ getTypeLabel(currentItem.type) }}</el-tag>
        <span v-if="currentItem.senderName"><i class="el-icon-user" /> {{ currentItem.senderName }}</span>
        <span><i class="el-icon-time" /> {{ formatDetailTime(currentItem.createTime) }}</span>
      </div>
      <div class="detail-content" v-html="currentItem.content" />
      <span slot="footer">
        <el-button v-if="currentItem.type === 'token_low'" type="primary" @click="goToToken">去充值</el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </span>
    </el-dialog>

    <message-popup ref="popup" @action="handlePopupAction" />
  </div>
</template>

<script>
import { getNotifications, getMustReadCount, markAsRead, markAllAsRead, getUnreadSummary } from '@/api/notification'
import { noticeGetNew, noticeMarkAsRead, noticeMarkAllAsRead, noticeGetUnreadCount } from '@/api/notice'
import MessagePopup from '@/components/MessagePopup/index.vue'

export default {
  name: 'MessageCenter',
  components: { MessagePopup },
  data() {
    return {
      loading: false,
      activeTab: 'all',
      pageNum: 1,
      pageSize: 10,
      total: 0,
      messages: [],
      allMessages: [],
      unreadSummary: {
        total: 0,
        notice: 0,
        friend_message: 0,
        token_low: 0,
        system_upgrade: 0,
        system: 0
      },
      mustReadCount: 0,
      detailVisible: false,
      currentItem: {}
    }
  },
  computed: {
    systemUnread() {
      return (this.unreadSummary.token_low || 0) + (this.unreadSummary.system_upgrade || 0) + (this.unreadSummary.system || 0)
    }
  },
  created() {
    this.fetchUnreadSummary()
    this.fetchData()
  },
  methods: {
    async fetchUnreadSummary() {
      try {
        const [summaryRes, mustReadRes, noticeRes] = await Promise.all([
          getUnreadSummary(),
          getMustReadCount(),
          noticeGetUnreadCount()
        ])
        if (summaryRes.code) {
          this.unreadSummary = summaryRes.data || {}
        }
        if (mustReadRes.code) this.mustReadCount = mustReadRes.data || 0
        // 公告未读数来自notice系统，避免与notification系统重复计数
        if (noticeRes.code && noticeRes.data > 0) {
          const noticeUnread = noticeRes.data || 0
          // 只在notification系统没有返回notice未读数时才补充
          if (!this.unreadSummary.notice) {
            this.unreadSummary.notice = noticeUnread
            this.unreadSummary.total = (this.unreadSummary.total || 0) + noticeUnread
          }
        }
      } catch (e) { /* ignore */ }
    },
    async fetchData() {
      this.loading = true
      try {
        let allMessages = []

        if (this.activeTab === 'notice') {
          // 公告tab：只从notice系统获取，避免重复
          const res = await noticeGetNew({ pageNum: 1, pageSize: 50 })
          if (res.code) {
            allMessages = (res.data.records || []).map(item => ({
              ...item,
              type: 'notice',
              isRead: item.isRead ? 1 : 0
            }))
          }
        } else if (this.activeTab === 'all') {
          // 全部tab：分别获取notice和其他通知，合并去重
          const [noticeRes, notifRes] = await Promise.all([
            noticeGetNew({ pageNum: 1, pageSize: 50 }),
            getNotifications({ pageNum: 1, pageSize: 50 })
          ])
          const notices = []
          if (noticeRes.code) {
            notices.push(...(noticeRes.data.records || []).map(item => ({
              ...item,
              type: 'notice',
              isRead: item.isRead ? 1 : 0
            })))
          }
          const notifications = []
          if (notifRes.code) {
            notifRes.data.records.forEach(item => {
              // 排除notice类型，避免与notice系统重复
              if (item.type !== 'notice') {
                notifications.push(item)
              }
            })
          }
          allMessages = [...notices, ...notifications]
        } else if (this.activeTab === 'system') {
          // 系统tab：获取token_low + system_upgrade + system类型
          const res = await getNotifications({ pageNum: 1, pageSize: 50 })
          if (res.code) {
            allMessages = (res.data.records || []).filter(
              item => ['token_low', 'system_upgrade', 'system'].includes(item.type)
            )
          }
        } else {
          // 其他tab（好友消息等）
          const res = await getNotifications({ pageNum: 1, pageSize: 50, type: this.activeTab })
          if (res.code) {
            allMessages = res.data.records || []
          }
        }

        allMessages.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
        this.allMessages = allMessages
        this.total = allMessages.length
        const start = (this.pageNum - 1) * this.pageSize
        this.messages = allMessages.slice(start, start + this.pageSize)
      } catch (e) {
        this.$message.error('获取消息失败')
      } finally {
        this.loading = false
      }
    },
    handleTabChange() {
      this.pageNum = 1
      this.fetchData()
    },
    handlePageChange(page) {
      this.pageNum = page
      const start = (this.pageNum - 1) * this.pageSize
      this.messages = this.allMessages.slice(start, start + this.pageSize)
    },
    async handleItemClick(item) {
      this.currentItem = item
      this.detailVisible = true

      if (item.isRead === 0) {
        try {
          if (item.type === 'notice') {
            await noticeMarkAsRead(item.id)
          } else {
            await markAsRead(item.id)
          }
          item.isRead = 1
          this.fetchUnreadSummary()
        } catch (e) { /* ignore */ }
      }
    },
    async handleMarkAllRead() {
      try {
        await Promise.all([
          noticeMarkAllAsRead(),
          markAllAsRead()
        ])
        this.$message.success('已全部标记为已读')
        this.mustReadCount = 0
        this.unreadSummary = { total: 0, notice: 0, friend_message: 0, token_low: 0, system_upgrade: 0, system: 0 }
        this.fetchData()
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    handlePopupAction(msg) {
      switch (msg.type) {
        case 'token_low':
          this.$router.push('/token')
          break
        case 'friend_message':
        case 'friend_request':
          this.$router.push('/chat')
          break
        default:
          this.$router.push('/notifications')
      }
    },
    goToToken() {
      this.detailVisible = false
      this.$router.push('/token')
    },
    getIconClass(type) {
      const classes = {
        notice: 'icon-notice',
        system: 'icon-system',
        friend_request: 'icon-friend',
        friend_message: 'icon-message',
        grading_complete: 'icon-grading',
        token_low: 'icon-token',
        system_upgrade: 'icon-upgrade'
      }
      return classes[type] || 'icon-system'
    },
    getIcon(type) {
      const icons = {
        notice: 'el-icon-s-order',
        system: 'el-icon-s-tools',
        friend_request: 'el-icon-user',
        friend_message: 'el-icon-chat-dot-round',
        grading_complete: 'el-icon-document-checked',
        token_low: 'el-icon-coin',
        system_upgrade: 'el-icon-upload'
      }
      return icons[type] || 'el-icon-bell'
    },
    getTypeLabel(type) {
      const types = {
        notice: '公告',
        system: '系统通知',
        friend_request: '好友请求',
        friend_message: '好友消息',
        grading_complete: '阅卷完成',
        token_low: 'Token不足',
        system_upgrade: '系统升级'
      }
      return types[type] || '通知'
    },
    getTypeTag(type) {
      const tags = {
        notice: 'info',
        system: '',
        friend_request: 'warning',
        friend_message: 'success',
        grading_complete: 'success',
        token_low: 'danger',
        system_upgrade: ''
      }
      return tags[type] || 'info'
    },
    getExcerpt(content) {
      if (!content) return ''
      const text = content.replace(/<[^>]*>/g, '')
      return text.length > 60 ? text.substring(0, 60) + '...' : text
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
    }
  }
}
</script>

<style lang="scss" scoped>
.message-center {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .header-left {
    .page-title {
      font-size: 20px;
      font-weight: 600;
      margin: 0 0 4px;
      color: var(--text-primary);
    }

    .page-subtitle {
      margin: 0;
      font-size: 13px;
      color: var(--text-secondary);
    }
  }
}

.filter-tabs {
  margin-bottom: 16px;

  .tab-count {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    margin-left: 4px;
    font-size: 10px;
    font-weight: 600;
    color: #fff;
    background: #f56c6c;
    border-radius: 8px;
    line-height: 1;
  }
}

.must-read-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 8px;
  margin-bottom: 16px;
  color: #f5222d;
  font-size: 13px;

  i { font-size: 16px; }
}

.message-list {
  min-height: 200px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: var(--glass-bg, #fff);
  border: 1px solid var(--glass-border, #e8e8e8);
  border-radius: 10px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;

  &:hover {
    border-color: var(--accent, #6366F1);
    box-shadow: 0 2px 8px rgba(99, 102, 241, 0.1);
  }

  &.unread {
    background: rgba(99, 102, 241, 0.04);
    border-color: rgba(99, 102, 241, 0.2);
  }

  &.must-read {
    border-left: 3px solid #f5222d;
  }

  .item-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    flex-shrink: 0;

    &.icon-notice { background: #e6f7ff; color: #1890ff; }
    &.icon-system { background: #f0f0f0; color: #666; }
    &.icon-friend { background: #fff7e6; color: #fa8c16; }
    &.icon-message { background: #f9f0ff; color: #722ed1; }
    &.icon-grading { background: #f6ffed; color: #52c41a; }
    &.icon-token { background: #fff1f0; color: #f5222d; }
    &.icon-upgrade { background: #e6fffb; color: #13c2c2; }
  }

  .item-body {
    flex: 1;
    min-width: 0;

    .item-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;

      .item-title {
        font-size: 14px;
        font-weight: 500;
        color: var(--text-primary, #333);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
        margin-right: 12px;
      }

      .item-time {
        font-size: 12px;
        color: var(--text-secondary, #999);
        flex-shrink: 0;
      }
    }

    .item-excerpt {
      font-size: 13px;
      color: var(--text-secondary, #666);
      line-height: 1.4;
      margin-bottom: 6px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .item-bottom {
      display: flex;
      align-items: center;
      gap: 6px;

      .item-sender {
        font-size: 12px;
        color: var(--text-secondary, #999);
      }
    }
  }

  .unread-dot {
    width: 8px;
    height: 8px;
    background: #6366F1;
    border-radius: 50%;
    flex-shrink: 0;
    margin-top: 14px;
  }
}

.pagination {
  margin-top: 16px;
  text-align: center;
}

.message-detail-dialog {
  .detail-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    font-size: 13px;
    color: #999;

    i { margin-right: 4px; }
  }

  .detail-content {
    font-size: 14px;
    line-height: 1.8;
    color: #444;

    ::v-deep img {
      max-width: 100%;
      height: auto;
      border-radius: 8px;
    }
  }
}
</style>
