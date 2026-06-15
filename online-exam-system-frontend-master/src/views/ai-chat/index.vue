<template>
  <div class="app-container">
    <div class="ai-chat-container">
      <!-- 核心对话模块 -->
      <div class="chat-main">
        <!-- 功能栏（RAG控制） -->
        <div class="chat-header">
          <div class="rag-control-bar">
            <div class="rag-switch">
              <span class="rag-label">RAG智能增强搜索</span>
              <el-switch
                v-model="enableRag"
                @change="handleRagToggle"
              />
            </div>
            <div v-if="enableRag" class="rag-stats">
              📚 {{ knowledgeStats.totalMaterials }} 份资料
            </div>
            <div class="token-balance-badge" @click="$router.push('/token')">
              <i class="el-icon-coin" />
              <span>{{ tokenBalance }} Tokens</span>
              <i class="el-icon-plus token-add-icon" />
            </div>
          </div>
        </div>

        <!-- 对话区域 -->
        <div ref="messagesContainer" class="chat-messages">
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['message', message.role === 'user' ? 'user-message' : 'ai-message']"
          >
            <!-- AI消息：头像在左侧 -->
            <div v-if="message.role !== 'user'" class="message-avatar ai-avatar">
              <i class="el-icon-chat-line-round" />
            </div>
            <div class="message-content">
              <div class="message-text">{{ message.content }}</div>
              <div class="message-time">{{ message.time }}</div>
              <div v-if="message.ragEnabled" class="rag-tag">
                <el-tag size="mini" type="success">智能检索</el-tag>
              </div>
            </div>
            <!-- 用户消息：头像在右侧 -->
            <div v-if="message.role === 'user'" class="message-avatar user-avatar">
              <i class="el-icon-user" />
            </div>
          </div>
          <div v-if="isLoading" class="message ai-message">
            <div class="message-avatar ai-avatar">
              <i class="el-icon-chat-line-round" />
            </div>
            <div class="message-content">
              <div class="loading-text">
                {{ enableRag ? 'AI正在检索您的学习资料并思考中...' : 'AI正在思考中...' }}
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input-container">
          <el-input
            ref="inputRef"
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            :placeholder="enableRag ? '请输入您的问题，AI将基于您的学习资料进行解答...' : '请输入您的问题，AI将为您解答...'"
            :disabled="isLoading"
            class="chat-input"
            @keydown.enter.native="handleEnterKey"
          />
          <div class="input-hint">
            <span><kbd>Enter</kbd> 发送</span>
            <span><kbd>Shift</kbd> + <kbd>Enter</kbd> 换行</span>
          </div>
          <div class="input-actions">
            <el-button
              type="primary"
              :loading="isLoading"
              class="send-button"
              @click="handleSendMessage"
            >
              {{ enableRag ? '智能提问' : '发送' }}
            </el-button>
            <el-button
              :disabled="isLoading"
              class="clear-button"
              @click="clearMessages"
            >
              清空对话
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { aiChat } from '@/api/ai-chat'
import { aiChatHistory, aiChatHistoryClear } from '@/api/ai-chat-history'
import { enhancedAiChat } from '@/api/ai-chat-enhanced'
import { getKnowledgeStats } from '@/api/rag'
import { getTokenBalance } from '@/api/token'
import { mapGetters } from 'vuex'

export default {
  name: 'AiChat',
  data() {
    return {
      inputMessage: '',
      messages: [],
      isLoading: false,
      hasLoadedHistory: false,
      enableRag: true,
      knowledgeStats: {
        totalChunks: 0,
        totalMaterials: 0,
        status: '未启用'
      },
      tokenBalance: 0
    }
  },

  computed: {
    ...mapGetters(['aiConfigId']),
    selectedConfigId() {
      return this.aiConfigId || (this.$store.state.aiModel ? this.$store.state.aiModel.selectedConfigId : null)
    }
  },

  mounted() {
    this.$store.dispatch('aiModel/loadModels')
    this.loadChatHistory()
    this.loadKnowledgeStats()
    this.loadTokenBalance()
    this.addWelcomeMessage()
  },

  methods: {
    async loadChatHistory() {
      try {
        const res = await aiChatHistory()
        if (res.code && res.data) {
          this.messages = res.data
          this.hasLoadedHistory = true
        }
      } catch (error) {
        console.error('加载聊天记录失败:', error)
      }
    },

    async loadKnowledgeStats() {
      if (!this.enableRag) return

      try {
        const res = await getKnowledgeStats()
        if (res.code && res.data) {
          this.knowledgeStats = res.data
        }
      } catch (error) {
        console.error('加载知识库统计失败:', error)
      }
    },

    async loadTokenBalance() {
      try {
        const res = await getTokenBalance()
        if (res.code && res.data) {
          this.tokenBalance = res.data.balance
        }
      } catch (error) {
        console.error('加载Token余额失败:', error)
      }
    },

    addWelcomeMessage() {
      if (this.messages.length === 0) {
        const welcomeMessage = {
          role: 'ai',
          content: this.enableRag
            ? '您好！我是您的AI学习助手，可以基于您的学习资料为您提供智能解答。请开始提问吧！'
            : '您好！我是您的AI学习助手，请开始提问吧！',
          time: this.getCurrentTime(),
          ragEnabled: this.enableRag
        }
        this.messages.push(welcomeMessage)
      }
    },

    handleRagToggle(value) {
      this.enableRag = value
      this.loadKnowledgeStats()
      if (this.messages.length === 1) {
        this.messages[0].content = value
          ? '您好！我是您的AI学习助手，可以基于您的学习资料为您提供智能解答。请开始提问吧！'
          : '您好！我是您的AI学习助手，请开始提问吧！'
        this.messages[0].ragEnabled = value
      }
    },

    handleEnterKey(event) {
      if (event.shiftKey) {
        return
      }
      event.preventDefault()
      this.handleSendMessage()
    },

    async handleSendMessage() {
      if (!this.inputMessage.trim()) {
        this.$message.warning('请输入问题内容')
        return
      }

      if (this.isLoading) return

      if (this.tokenBalance <= 0) {
        this.$confirm('Token余额不足，请先充值后再使用AI服务', '余额不足', {
          confirmButtonText: '去充值',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$router.push('/token')
        }).catch(() => {})
        return
      }

      // 添加用户消息
      const userMessage = {
        role: 'user',
        content: this.inputMessage.trim(),
        time: this.getCurrentTime(),
        ragEnabled: this.enableRag
      }
      this.messages.push(userMessage)

      const question = this.inputMessage.trim()
      this.inputMessage = ''
      this.isLoading = true

      try {
        let response
        const configId = this.selectedConfigId
        if (this.enableRag) {
          response = await enhancedAiChat({ question, configId })
        } else {
          response = await aiChat({ question, configId })
        }

        if (response.code && response.data) {
          const aiMessage = {
            role: 'ai',
            content: response.data,
            time: this.getCurrentTime(),
            ragEnabled: this.enableRag
          }
          this.messages.push(aiMessage)
          this.loadTokenBalance()
        } else {
          throw new Error(response.msg || 'AI服务返回错误')
        }
      } catch (error) {
        console.error('发送消息失败:', error)
        const errorMsg = error.message || '未知错误'
        if (errorMsg.includes('Token余额不足')) {
          this.$confirm('Token余额不足，请先充值后再使用AI服务', '余额不足', {
            confirmButtonText: '去充值',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.$router.push('/token')
          }).catch(() => {})
        }
        const errorMessage = {
          role: 'ai',
          content: '抱歉，AI服务暂时不可用，请稍后重试。',
          time: this.getCurrentTime(),
          ragEnabled: this.enableRag,
          isError: true
        }
        this.messages.push(errorMessage)
        this.$message.error('发送失败：' + errorMsg)
      } finally {
        this.isLoading = false
        this.scrollToBottom()
      }
    },

    async clearMessages() {
      try {
        await this.$confirm('确定要清空所有聊天记录吗？此操作不可恢复。', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        const res = await aiChatHistoryClear()
        if (res.code) {
          this.$message.success('聊天记录已清空')
          this.messages = []
          this.addWelcomeMessage()
        } else {
          this.$message.error('清空失败：' + res.msg)
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('清空聊天记录失败:', error)
          this.$message.error('清空失败')
        }
      }
    },

    getCurrentTime() {
      const now = new Date()
      return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  min-height: calc(100vh - 84px);
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.ai-chat-container {
  width: 95%;
  max-width: 1400px;
  margin: 0 auto;
}

.chat-main {
  background: white;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  border: 1px solid #f1f5f9;
  min-height: 700px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  padding: 20px 32px;
  border-bottom: 1px solid #f1f5f9;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.rag-control-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.rag-switch {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 16px;
  background: rgba(64, 158, 255, 0.05);
  border-radius: 10px;
  border: 1px solid rgba(64, 158, 255, 0.1);
}

.rag-label {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
  letter-spacing: 0.5px;
}

.rag-stats {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
  background: rgba(100, 116, 139, 0.08);
  padding: 4px 8px;
  border-radius: 4px;
  white-space: nowrap;
}

.token-balance-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: linear-gradient(135deg, #e6a23c, #f0c78a);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.3);
}

.token-balance-badge:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.4);
}

.token-add-icon {
  font-size: 10px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-messages {
  flex: 1;
  min-height: 500px;
  max-height: 70vh;
  overflow-y: auto;
  padding: 32px;
  background: #fafbfc;
}

.message {
  display: flex;
  margin-bottom: 28px;
  align-items: flex-start;
}

.user-message {
  justify-content: flex-end;
}

.ai-message {
  justify-content: flex-start;
}

.message-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.ai-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  margin-right: 16px;
  margin-left: 0;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.user-avatar {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: white;
  margin-left: 16px;
  margin-right: 0;
  order: 2;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.message-content {
  max-width: 80%;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.user-message .message-content {
  align-items: flex-end;
  order: 1;
}

.ai-message .message-content {
  align-items: flex-start;
}

.message-text {
  padding: 18px 22px;
  border-radius: 16px;
  word-break: break-word;
  line-height: 1.7;
  font-size: 15px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.user-message .message-text {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: white;
  border-bottom-right-radius: 6px;
  margin-left: 60px;
}

.ai-message .message-text {
  background: white;
  color: #334155;
  border: 1px solid #e2e8f0;
  border-bottom-left-radius: 6px;
  margin-right: 60px;
}

.message-time {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 8px;
  text-align: right;
  font-weight: 500;
}

.ai-message .message-time {
  text-align: left;
}

.rag-tag {
  margin-top: 8px;
}

.loading-text {
  color: #64748b;
  font-style: italic;
  font-size: 15px;
  padding: 20px;
  text-align: center;
}

.chat-input-container {
  padding: 24px 32px;
  border-top: 1px solid #f1f5f9;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.chat-input {
  margin-bottom: 12px;
}

.input-hint {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
  font-size: 12px;
  color: #909399;
}

.input-hint kbd {
  display: inline-block;
  padding: 2px 6px;
  font-size: 11px;
  font-family: Consolas, Monaco, monospace;
  line-height: 1;
  color: #606266;
  background-color: #f0f2f5;
  border: 1px solid #dcdfe6;
  border-radius: 3px;
  box-shadow: inset 0 -1px 0 #dcdfe6;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.send-button {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border: none;
  border-radius: 10px;
  padding: 12px 28px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.send-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

.clear-button {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px 28px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
}

.clear-button:hover {
  border-color: #409eff;
  color: #409eff;
  transform: translateY(-2px);
}
</style>
