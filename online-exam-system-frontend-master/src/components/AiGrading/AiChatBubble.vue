<template>
  <div v-if="visible" class="ai-chat-bubble" :style="bubbleStyle">
    <div class="bubble-header">
      <span class="bubble-title">
        <i class="el-icon-chat-dot-round" />
        AI题目答疑
      </span>
      <el-button type="text" size="mini" class="bubble-close" @click="handleClose">
        <i class="el-icon-close" />
      </el-button>
    </div>

    <div v-if="questionTitle" class="bubble-context">
      <div class="context-label">当前题目</div>
      <div class="context-text">{{ questionTitle | truncate }}</div>
    </div>

    <div class="bubble-messages" ref="messagesContainer">
      <div v-if="messages.length === 0" class="bubble-empty">
        <i class="el-icon-chat-line-square" style="font-size: 28px; color: #c0c4cc"></i>
        <p>针对此题目提问，AI将为你解答</p>
      </div>
      <div v-for="(msg, idx) in messages" :key="idx"
        :class="['bubble-msg', msg.role === 'user' ? 'msg-user' : 'msg-ai']">
        <div class="msg-avatar">
          <i :class="msg.role === 'user' ? 'el-icon-user-solid' : 'el-icon-cpu'" />
        </div>
        <div class="msg-content">
          <div class="msg-text">{{ msg.content }}</div>
        </div>
      </div>
      <div v-if="loading" class="bubble-msg msg-ai">
        <div class="msg-avatar">
          <i class="el-icon-cpu" />
        </div>
        <div class="msg-content">
          <div class="msg-text typing">
            <i class="el-icon-loading" /> AI正在思考...
          </div>
        </div>
      </div>
    </div>

    <div class="bubble-input">
      <el-input
        v-model="inputText"
        placeholder="输入你的问题..."
        size="small"
        @keyup.enter.native="sendMessage"
        :disabled="loading"
      >
        <el-button slot="append" icon="el-icon-s-promotion" @click="sendMessage" :loading="loading" />
      </el-input>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'
import { getUserId } from '@/utils/auth'

export default {
  name: 'AiChatBubble',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    questionTitle: {
      type: String,
      default: ''
    },
    standardAnswer: {
      type: String,
      default: ''
    },
    userAnswer: {
      type: String,
      default: ''
    },
    configId: {
      type: Number,
      default: null
    }
  },
  filters: {
    truncate(val) {
      if (!val) return ''
      return val.length > 80 ? val.substring(0, 80) + '...' : val
    }
  },
  data() {
    return {
      inputText: '',
      messages: [],
      loading: false
    }
  },
  computed: {
    bubbleStyle() {
      return {}
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.messages = []
        this.inputText = ''
      }
    }
  },
  methods: {
    async sendMessage() {
      const text = this.inputText.trim()
      if (!text || this.loading) return

      this.messages.push({ role: 'user', content: text })
      this.inputText = ''
      this.loading = true
      this.scrollToBottom()

      try {
        console.log('[AiChatBubble] 发送AI问答, configId=', this.configId)
        const res = await request({
          url: '/ai/chat/question',
          method: 'post',
          data: {
            question: text,
            questionContent: this.questionTitle,
            standardAnswer: this.standardAnswer,
            userAnswer: this.userAnswer,
            configId: this.configId
          }
        })

        if (res && res.code === 1) {
          this.messages.push({ role: 'ai', content: res.data })
        } else {
          this.messages.push({ role: 'ai', content: res?.msg || 'AI服务暂不可用，请稍后重试' })
        }
      } catch (e) {
        console.error('AI问答异常:', e)
        this.messages.push({ role: 'ai', content: 'AI服务暂不可用，请稍后重试' })
      } finally {
        this.loading = false
        this.scrollToBottom()
      }
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    },
    handleClose() {
      this.messages = []
      this.inputText = ''
      this.$emit('close')
    }
  }
}
</script>

<style scoped>
.ai-chat-bubble {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 380px;
  height: 520px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  z-index: 3000;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

.bubble-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
  flex-shrink: 0;
}

.bubble-title {
  font-size: 15px;
  font-weight: 600;
}

.bubble-title i {
  margin-right: 6px;
}

.bubble-close {
  color: #fff !important;
  font-size: 16px;
  padding: 0;
}

.bubble-context {
  padding: 8px 14px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.context-label {
  font-size: 11px;
  color: #909399;
  margin-bottom: 3px;
}

.context-text {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  max-height: 40px;
  overflow: hidden;
}

.bubble-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px 14px;
}

.bubble-messages::-webkit-scrollbar {
  width: 4px;
}

.bubble-messages::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 2px;
}

.bubble-empty {
  text-align: center;
  padding: 40px 0;
  color: #c0c4cc;
}

.bubble-empty p {
  margin-top: 10px;
  font-size: 13px;
}

.bubble-msg {
  display: flex;
  margin-bottom: 14px;
}

.bubble-msg.msg-user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.msg-user .msg-avatar {
  background: #409eff;
  color: #fff;
  margin-left: 8px;
}

.msg-ai .msg-avatar {
  background: #f0f9ff;
  color: #409eff;
  margin-right: 8px;
}

.msg-content {
  max-width: 260px;
}

.msg-text {
  font-size: 13px;
  line-height: 1.6;
  padding: 8px 12px;
  border-radius: 10px;
  word-break: break-word;
  white-space: pre-wrap;
}

.msg-user .msg-text {
  background: #409eff;
  color: #fff;
  border-top-right-radius: 2px;
}

.msg-ai .msg-text {
  background: #f5f7fa;
  color: #303133;
  border-top-left-radius: 2px;
}

.msg-text.typing {
  color: #909399;
  font-style: italic;
}

.bubble-input {
  padding: 10px 12px;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}

.bubble-input .el-input-group__append {
  padding: 0 10px;
}
</style>