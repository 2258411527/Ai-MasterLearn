<template>
  <el-dialog
    :visible.sync="visible"
    :title="dialogTitle"
    width="480px"
    custom-class="message-popup-dialog"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="popup-content">
      <div class="popup-icon" :class="iconClass">
        <i :class="iconName" />
      </div>
      <div class="popup-body">
        <h3 class="popup-title">{{ message.title }}</h3>
        <p class="popup-text">{{ message.content }}</p>
        <div v-if="message.senderName" class="popup-sender">
          <el-avatar :size="24">{{ message.senderName.charAt(0) }}</el-avatar>
          <span>{{ message.senderName }}</span>
        </div>
        <div class="popup-time">{{ formatTime(message.createTime) }}</div>
      </div>
    </div>
    <div slot="footer" class="popup-footer">
      <el-button v-if="showIgnore" @click="handleIgnore">忽略</el-button>
      <el-button v-if="showAction" type="primary" @click="handleAction">{{ actionText }}</el-button>
      <el-button v-else @click="handleClose">关闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'MessagePopup',
  props: {
    message: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      visible: false
    }
  },
  computed: {
    dialogTitle() {
      const titles = {
        notice: '公告通知',
        system: '系统通知',
        friend_request: '好友请求',
        friend_message: '好友消息',
        grading_complete: '阅卷完成',
        token_low: 'Token不足',
        system_upgrade: '系统升级'
      }
      return titles[this.message.type] || '消息通知'
    },
    iconClass() {
      const classes = {
        notice: 'icon-notice',
        system: 'icon-system',
        friend_request: 'icon-friend',
        friend_message: 'icon-message',
        grading_complete: 'icon-grading',
        token_low: 'icon-token',
        system_upgrade: 'icon-upgrade'
      }
      return classes[this.message.type] || 'icon-system'
    },
    iconName() {
      const icons = {
        notice: 'el-icon-s-order',
        system: 'el-icon-s-tools',
        friend_request: 'el-icon-user',
        friend_message: 'el-icon-chat-dot-round',
        grading_complete: 'el-icon-document-checked',
        token_low: 'el-icon-coin',
        system_upgrade: 'el-icon-upload'
      }
      return icons[this.message.type] || 'el-icon-bell'
    },
    showAction() {
      return ['token_low', 'friend_request', 'friend_message'].includes(this.message.type)
    },
    showIgnore() {
      return this.message.type === 'friend_request'
    },
    actionText() {
      const texts = {
        token_low: '去充值',
        friend_request: '查看',
        friend_message: '回复'
      }
      return texts[this.message.type] || '查看'
    }
  },
  methods: {
    show() {
      this.visible = true
    },
    hide() {
      this.visible = false
    },
    handleClose() {
      this.$emit('close', this.message)
      this.hide()
    },
    handleIgnore() {
      this.$emit('ignore', this.message)
      this.hide()
    },
    handleAction() {
      this.$emit('action', this.message)
      this.hide()
    },
    formatTime(time) {
      if (!time) return ''
      const d = new Date(time)
      const now = new Date()
      const diff = now - d
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      return `${d.getMonth() + 1}-${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
    }
  }
}
</script>

<style lang="scss" scoped>
.message-popup-dialog {
  border-radius: 16px;

  .popup-content {
    display: flex;
    gap: 16px;
    padding: 10px 0;
  }

  .popup-icon {
    width: 56px;
    height: 56px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 26px;
    flex-shrink: 0;

    &.icon-notice { background: #e6f7ff; color: #1890ff; }
    &.icon-system { background: #f0f0f0; color: #666; }
    &.icon-friend { background: #fff7e6; color: #fa8c16; }
    &.icon-message { background: #f9f0ff; color: #722ed1; }
    &.icon-grading { background: #f6ffed; color: #52c41a; }
    &.icon-token { background: #fff1f0; color: #f5222d; }
    &.icon-upgrade { background: #e6fffb; color: #13c2c2; }
  }

  .popup-body {
    flex: 1;
    min-width: 0;
  }

  .popup-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px;
  }

  .popup-text {
    font-size: 14px;
    color: #666;
    line-height: 1.6;
    margin: 0 0 12px;
  }

  .popup-sender {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
    font-size: 13px;
    color: #666;

    .el-avatar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }
  }

  .popup-time {
    font-size: 12px;
    color: #999;
  }

  .popup-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}
</style>
