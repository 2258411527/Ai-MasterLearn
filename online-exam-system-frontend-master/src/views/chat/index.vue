<template>
  <div class="chat-container">
    <!-- 左侧好友列表 -->
    <div class="friend-sidebar">
      <div class="sidebar-header">
        <span class="header-title">好友</span>
        <div class="header-buttons">
          <el-badge :value="pendingCount" :hidden="pendingCount === 0">
            <el-button
              type="text"
              icon="el-icon-bell"
              title="好友请求"
              @click="showPendingRequests = true"
            />
          </el-badge>
          <el-button
            type="text"
            icon="el-icon-plus"
            title="添加好友"
            @click="showAddFriendDialog = true"
          />
          <!-- AI代理开关按钮 -->
          <el-button
            type="text"
            :icon="aiAgentEnabled ? 'el-icon-cpu' : 'el-icon-cpu'"
            :title="aiAgentEnabled ? 'AI代理已开启' : 'AI代理已关闭'"
            :class="{ 'ai-agent-btn': true, 'ai-agent-active': aiAgentEnabled }"
            @click="toggleAIAgent"
          />
        </div>
      </div>

      <!-- 好友搜索 -->
      <div class="search-box">
        <el-input
          v-model="friendSearch"
          placeholder="搜索好友"
          prefix-icon="el-icon-search"
          clearable
          size="small"
        />
      </div>

      <!-- 好友列表 -->
      <div class="friend-list">
        <div
          v-for="friend in friends"
          :key="friend.id"
          class="friend-item"
          :class="{ active: activeFriend && activeFriend.friendId === friend.friendId }"
          @click="selectFriend(friend)"
        >
          <div class="friend-avatar-wrapper">
            <img :src="formatAvatarUrl(friend.friendAvatar)" class="friend-avatar" alt="头像" @error="handleAvatarError">
          </div>
          <div class="friend-info">
            <div class="friend-name-wrapper">
              <div class="friend-name">{{ friend.displayName || '好友' }}</div>
            </div>
            <div class="last-message">{{ friend.lastMessage || '暂无消息' }}</div>
          </div>
        </div>

        <div v-if="filteredFriends.length === 0" class="empty-friends">
          <i class="el-icon-user" />
          <p>暂无好友</p>
        </div>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-main">
      <div v-if="activeFriend" class="chat-window">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="chat-user">
            <img :src="formatAvatarUrl(activeFriend.friendAvatar)" class="chat-avatar" alt="头像" @error="handleAvatarError">
            <div class="user-info">
              <div class="user-name">{{ activeFriend.displayName || '好友' }}</div>
            </div>
          </div>
          <div class="header-actions">
            <el-button
              type="text"
              icon="el-icon-edit"
              title="修改备注"
              class="action-btn"
              @click="showEditRemarkDialog"
            />
            <el-button
              type="text"
              icon="el-icon-delete"
              title="删除好友"
              class="action-btn delete-btn"
              @click="deleteFriend(activeFriend)"
            />
          </div>
        </div>

        <!-- 聊天消息区域 -->
        <div ref="messagesContainer" class="messages-area">
          <div
            v-for="message in messages"
            :key="message.id"
            class="message-item"
            :class="{ own: message.isOwn, other: !message.isOwn }"
          >
            <div class="msg-content">
              <div class="msg-bubble" :class="{ 'ai-message': message.isAI }">
                <span v-if="message.isAI" class="ai-badge">🤖 AI</span>
                {{ message.content }}
              </div>
              <div class="msg-time">{{ formatTime(message.createTime) }}</div>
            </div>
          </div>

          <div v-if="messages.length === 0" class="empty-messages">
            <i class="el-icon-chat-line-square" />
            <p>开始你们的对话吧</p>
          </div>
        </div>

        <!-- 消息输入区域 -->
        <div class="input-area">
          <el-input
            v-model="newMessage"
            type="textarea"
            :rows="3"
            placeholder="按 Enter 发送消息"
            resize="none"
            @keydown.enter.native.prevent="sendMessage"
          />
          <div class="input-actions">
            <el-button
              type="primary"
              size="small"
              :disabled="!newMessage.trim()"
              @click="sendMessage"
            >
              发送
            </el-button>
          </div>
        </div>
      </div>

      <!-- 没有选择好友时的提示 -->
      <div v-else class="empty-chat">
        <i class="el-icon-chat-dot-round" />
        <p>选择一个好友开始聊天</p>
      </div>
    </div>

    <!-- 添加好友对话框 -->
    <el-dialog
      title="添加好友"
      :visible.sync="showAddFriendDialog"
      width="450px"
      class="add-friend-dialog"
    >
      <div class="dialog-content">
        <el-input
          v-model="searchUserName"
          placeholder="输入用户名或姓名"
          prefix-icon="el-icon-search"
          clearable
          @keydown.enter.native="searchUser"
        />
        <el-button
          type="primary"
          class="search-btn"
          @click="searchUser"
        >
          搜索
        </el-button>

        <div v-if="searchResult" class="search-result">
          <img :src="searchResult.avatar || '/default-avatar.png'" class="result-avatar" alt="头像">
          <div class="result-info">
            <div class="result-name">{{ searchResult.realName }}</div>
            <div class="result-username">@{{ searchResult.userName }}</div>
          </div>
          <el-button
            type="primary"
            :disabled="isAlreadyFriend"
            @click="sendFriendRequest"
          >
            {{ isAlreadyFriend ? '已是好友' : '添加' }}
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 待处理好友请求对话框 -->
    <el-dialog
      title="好友请求"
      :visible.sync="showPendingRequests"
      width="500px"
      class="pending-dialog"
    >
      <div v-if="pendingRequests.length === 0" class="empty-state">
        <i class="el-icon-bell" />
        <p>暂无待处理的请求</p>
      </div>
      <div v-else class="request-list">
        <div
          v-for="request in pendingRequests"
          :key="request.id"
          class="request-item"
        >
          <img :src="request.userAvatar || '/default-avatar.png'" class="req-avatar" alt="头像">
          <div class="req-info">
            <div class="req-name">{{ request.userName }}</div>
            <div class="req-time">{{ formatTime(request.createTime) }}</div>
          </div>
          <div class="req-actions">
            <el-button
              type="success"
              size="mini"
              @click="handleRequest(request.id, 1)"
            >
              同意
            </el-button>
            <el-button
              type="info"
              size="mini"
              @click="handleRequest(request.id, 2)"
            >
              拒绝
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 修改备注对话框 -->
    <el-dialog
      title="修改备注"
      :visible.sync="showEditRemarkDialogVisible"
      width="400px"
      class="edit-remark-dialog"
    >
      <el-form :model="remarkForm" label-width="80px">
        <el-form-item label="好友昵称">
          <span>{{ currentEditFriend ? currentEditFriend.displayName : '' }}</span>
        </el-form-item>
        <el-form-item label="备注名称">
          <el-input
            v-model="remarkForm.remark"
            placeholder="请输入备注名称（可选）"
            maxlength="20"
            show-word-limit
            clearable
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showEditRemarkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEditRemark">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getFriendList, searchUser, sendFriendRequest, deleteFriend, getChatHistory, sendMessage as sendChatMessage, markMessagesAsRead, getPendingRequests, handleFriendRequest, updateFriendRemark } from '@/api/chat'
import { aiChat } from '@/api/ai-chat'
import { getUserId, getRole } from '@/utils/auth'
import { mapGetters } from 'vuex'

export default {
  name: 'Chat',
  data() {
    return {
      friends: [],
      activeFriend: null,
      messages: [],
      newMessage: '',
      friendSearch: '',
      showAddFriendDialog: false,
      showPendingRequests: false,
      pendingRequests: [],
      searchUserName: '',
      searchResult: null,
      currentUser: {},

      // WebSocket相关
      socket: null,
      isConnected: false,

      // 头像缓存
      avatarCache: new Map(),

      // 定时轮询相关
      pollingTimer: null,
      lastMessageCheckTime: {},

      // 修改备注相关
      showEditRemarkDialogVisible: false,
      currentEditFriend: null,
      remarkForm: {
        remark: ''
      },

      // AI助手相关
      aiReplyLoading: false,

      // AI代理状态
      aiAgentEnabled: false
    }
  },
  computed: {
    ...mapGetters(['aiConfigId']),
    selectedConfigId() {
      return this.aiConfigId || (this.$store.state.aiModel ? this.$store.state.aiModel.selectedConfigId : null)
    },
    filteredFriends() {
      if (!this.friendSearch) return this.friends
      return this.friends.filter(friend =>
        friend.friendName && friend.friendName.toLowerCase().includes(this.friendSearch.toLowerCase())
      )
    },
    isAlreadyFriend() {
      if (!this.searchResult) return false
      return this.friends.some(friend => friend.friendId === this.searchResult.id)
    },
    pendingCount() {
      return this.pendingRequests.length
    }
  },
  async created() {
    this.$store.dispatch('aiModel/loadModels')
    const userId = getUserId()
    const role = getRole()
    if (userId) {
      this.currentUser = {
        id: parseInt(userId),
        role: role
      }
    }

    await this.loadFriends()
    await this.loadPendingRequests()
    this.initWebSocket()
    this.startPolling() // 启动定时轮询
  },
  beforeDestroy() {
    if (this.socket) {
      this.socket.close()
    }
    this.stopPolling() // 停止定时轮询
  },
  methods: {
    // 加载好友列表
    async loadFriends() {
      try {
        console.log('开始加载好友列表，当前用户ID:', this.currentUser?.id)
        const res = await getFriendList()
        console.log('好友列表API响应:', res)

        if (res.code) {
          this.friends = res.data || []
          console.log('好友列表原始数据:', this.friends)

          // 调试好友字段
          if (this.friends.length > 0) {
            console.log('第一个好友的字段结构:', Object.keys(this.friends[0]))
            this.friends.forEach((friend, index) => {
              console.log(`好友${index}:`, {
                friendName: friend.friendName,
                userName: friend.userName,
                realName: friend.realName,
                friend_name: friend.friend_name,
                user_name: friend.user_name
              })
            })
          }

          // 处理好友名称显示
          this.friends = this.friends.map(friend => ({
            ...friend,
            displayName: friend.friendName || friend.userName || friend.realName ||
                        friend.friend_name || friend.user_name || '好友'
          }))

          // 后端已经处理了displayName，直接使用
          console.log('处理后的好友列表:', this.friends)

          // 预加载好友头像
          this.preloadFriendAvatars()
        } else {
          console.error('加载好友列表失败:', res.msg)
          this.$message.error(res.msg || '加载好友列表失败')
        }
      } catch (error) {
        console.error('加载好友列表异常:', error)
        this.$message.error('加载好友列表失败')
      }
    },

    // 预加载好友头像
    preloadFriendAvatars() {
      this.friends.forEach(friend => {
        const avatarUrl = this.formatAvatarUrl(friend.friendAvatar)
        if (avatarUrl && !avatarUrl.startsWith('data:')) {
          this.preloadImage(avatarUrl)
        }
      })
    },

    // 预加载图片
    preloadImage(url) {
      return new Promise((resolve) => {
        const img = new Image()
        img.onload = () => {
          console.log('头像预加载成功:', url)
          resolve(true)
        }
        img.onerror = () => {
          console.log('头像预加载失败:', url)
          resolve(false)
        }
        img.src = url
      })
    },

    // 选择好友
    selectFriend(friend) {
      this.activeFriend = {
        ...friend
      }
      this.loadMessages(friend.friendId)
      this.$nextTick(() => {
        this.scrollToBottom()
      })
    },

    // 加载聊天记录
    async loadMessages(friendId) {
      try {
        const res = await getChatHistory(friendId, 50)
        if (res.code) {
          this.messages = (res.data || []).map(msg => ({
            id: msg.id,
            content: msg.content,
            createTime: msg.createTime,
            isOwn: msg.fromUserId === this.currentUser.id,
            fromUserId: msg.fromUserId,
            toUserId: msg.toUserId
          }))

          // 标记消息为已读
          await markMessagesAsRead(friendId).catch(err => {
            console.error('标记已读失败:', err)
          })

          this.$nextTick(() => {
            this.scrollToBottom()
          })
        } else {
          this.$message.error(res.msg || '加载聊天记录失败')
        }
      } catch (error) {
        console.error('加载聊天记录失败:', error)
        this.$message.error('加载聊天记录失败')
      }
    },

    // 发送消息
    async sendMessage() {
      if (!this.newMessage.trim() || !this.activeFriend) return

      try {
        const res = await sendChatMessage({
          toUserId: this.activeFriend.friendId,
          content: this.newMessage.trim(),
          messageType: 1 // 文本消息
        })

        if (res.code) {
          // 添加到本地消息列表
          const newMessage = {
            id: res.data.id,
            content: res.data.content,
            createTime: res.data.createTime,
            isOwn: true,
            fromUserId: this.currentUser.id,
            toUserId: this.activeFriend.friendId
          }

          this.messages.push(newMessage)
          this.newMessage = ''

          // 更新最后一条消息显示
          if (this.activeFriend) {
            this.$set(this.activeFriend, 'lastMessage', newMessage.content)
          }

          // 通过WebSocket通知对方
          if (this.socket && this.isConnected) {
            this.socket.send(JSON.stringify({
              type: 'CHAT_MESSAGE',
              data: newMessage
            }))
          }

          this.$nextTick(() => {
            this.scrollToBottom()
          })
        } else {
          this.$message.error(res.msg || '发送失败')
        }
      } catch (error) {
        console.error('发送消息失败:', error)
        this.$message.error('发送失败')
      }
    },

    // 滚动到底部
    scrollToBottom() {
      if (this.$refs.messagesContainer) {
        this.$refs.messagesContainer.scrollTop = this.$refs.messagesContainer.scrollHeight
      }
    },

    // 初始化WebSocket
    initWebSocket() {
      if (!this.currentUser || !this.currentUser.id) {
        console.error('无法初始化WebSocket: 用户信息为空')
        return
      }

      const userId = this.currentUser.id
      const wsUrl = `ws://localhost:8080/api/websocket?userId=${userId}`

      try {
        this.socket = new WebSocket(wsUrl)

        this.socket.onopen = () => {
          this.isConnected = true
          console.log('WebSocket连接成功, userId:', userId)
        }

        this.socket.onmessage = (event) => {
          try {
            console.log('收到WebSocket原始数据:', event.data)
            const message = JSON.parse(event.data)
            this.handleIncomingMessage(message)
          } catch (error) {
            console.error('解析WebSocket消息失败:', error, '原始数据:', event.data)
          }
        }

        this.socket.onclose = (event) => {
          this.isConnected = false
          console.log('WebSocket连接关闭:', event.code, event.reason)

          // 3秒后尝试重连
          setTimeout(() => {
            if (!this.isConnected) {
              console.log('尝试重新连接WebSocket...')
              this.initWebSocket()
            }
          }, 3000)
        }

        this.socket.onerror = (error) => {
          console.error('WebSocket错误:', error)
        }
      } catch (error) {
        console.error('WebSocket初始化失败:', error)
      }
    },

    // 处理接收到的消息
    handleIncomingMessage(message) {
      console.log('收到WebSocket消息:', message)

      // 检查消息格式是否正确
      if (!message.data) {
        console.warn('消息格式不正确:', message)
        return
      }

      const messageData = message.data

      // 处理AI代回复请求
      if (message.type === 'AI_REPLY_REQUEST') {
        this.handleAIReplyRequest(messageData)
        return
      }

      // 处理普通聊天消息
      if (!messageData.fromUserId) {
        console.warn('消息格式不正确:', message)
        return
      }

      // 如果当前正在和发送者聊天，直接添加到消息列表
      if (this.activeFriend && this.activeFriend.friendId === messageData.fromUserId) {
        this.messages.push({
          ...messageData,
          isOwn: false
        })

        // 标记为已读
        markMessagesAsRead(messageData.fromUserId).catch(err => {
          console.error('标记已读失败:', err)
        })

        this.$nextTick(() => {
          this.scrollToBottom()
        })
      } else {
        // 如果不在当前聊天窗口，更新好友列表的最后一条消息
        const friendIndex = this.friends.findIndex(f => f.friendId === messageData.fromUserId)
        if (friendIndex !== -1) {
          // 更新最后一条消息
          this.$set(this.friends[friendIndex], 'lastMessage', messageData.content)

          // 将该好友移到列表顶部
          const friend = this.friends.splice(friendIndex, 1)[0]
          this.friends.unshift(friend)
        }

        // 显示通知
        const friendName = this.getFriendName(messageData.fromUserId)
        this.$notify({
          title: '新消息',
          message: `${friendName}: ${messageData.content}`,
          type: 'info',
          duration: 3000
        })
      }

      // 如果开启了AI代理，自动回复（无论在线状态）
      if (this.aiAgentEnabled) {
        console.log('AI代理已开启，自动回复消息')
        this.autoAIReply(messageData.fromUserId, messageData.content)
      }
    },

    // 自动AI回复
    async autoAIReply(friendId, message) {
      console.log('自动AI回复:', { friendId, message })

      try {
        // 调用AI接口获取回复
        const aiRes = await aiChat({
          question: message,
          configId: this.selectedConfigId
        })

        if (aiRes.code && aiRes.data) {
          console.log('AI自动回复成功:', aiRes.data)

          // 用AI的回答回复对方
          const replyMessage = {
            fromUserId: this.currentUser.id,
            toUserId: friendId,
            content: aiRes.data,
            messageType: 1,
            isRead: 0
          }

          const replyRes = await sendChatMessage(replyMessage)

          if (replyRes.code) {
            // 将AI回复添加到聊天窗口（如果当前正在与该用户聊天）
            if (this.activeFriend && this.activeFriend.friendId === friendId) {
              const aiReply = {
                id: replyRes.data.id,
                content: replyRes.data.content,
                createTime: replyRes.data.createTime,
                isOwn: true,
                fromUserId: this.currentUser.id,
                toUserId: friendId,
                isAI: true // 标记为AI代回复
              }

              this.messages.push(aiReply)
              this.$nextTick(() => {
                this.scrollToBottom()
              })
            }

            // 通过WebSocket通知对方
            if (this.socket && this.isConnected) {
              this.socket.send(JSON.stringify({
                type: 'CHAT_MESSAGE',
                data: {
                  id: replyRes.data.id,
                  content: replyRes.data.content,
                  createTime: replyRes.data.createTime,
                  fromUserId: this.currentUser.id,
                  toUserId: friendId,
                  isAI: true
                }
              }))
            }

            console.log('AI自动回复成功发送')
          } else {
            console.error('发送AI自动回复失败:', replyRes.msg)
          }
        } else {
          console.error('AI生成自动回复失败:', aiRes.msg)
        }
      } catch (error) {
        console.error('处理AI自动回复失败:', error)
      }
    },

    // 切换AI代理状态
    toggleAIAgent() {
      this.aiAgentEnabled = !this.aiAgentEnabled

      if (this.aiAgentEnabled) {
        this.$message.success('AI代理已开启，好友消息将由AI自动回复')
        console.log('AI代理已开启')
      } else {
        this.$message.info('AI代理已关闭')
        console.log('AI代理已关闭')
      }

      this.$forceUpdate()
    },

    // 处理AI代回复请求
    async handleAIReplyRequest(requestData) {
      console.log('收到AI代回复请求:', requestData)

      // 检查是否在线状态
      if (!this.userOnlineStatus) {
        this.$message.warning('请先上线才能处理AI代回复请求')
        return
      }

      try {
        // 显示处理中的提示
        this.$message.info('AI代回复处理中...')

        // 调用AI接口获取回复
        const aiRes = await aiChat({
          question: requestData.question,
          configId: this.selectedConfigId
        })

        if (aiRes.code && aiRes.data) {
          console.log('AI回复成功:', aiRes.data)

          // 用AI的回答回复对方
          const replyMessage = {
            fromUserId: this.currentUser.id,
            toUserId: requestData.fromUserId,
            content: aiRes.data,
            messageType: 1,
            isRead: 0
          }

          const replyRes = await sendChatMessage(replyMessage)

          if (replyRes.code) {
            // 将AI回复添加到聊天窗口（如果当前正在与该用户聊天）
            if (this.activeFriend && this.activeFriend.friendId === requestData.fromUserId) {
              const aiReply = {
                id: replyRes.data.id,
                content: replyRes.data.content,
                createTime: replyRes.data.createTime,
                isOwn: true,
                fromUserId: this.currentUser.id,
                toUserId: requestData.fromUserId,
                isAI: true // 标记为AI代回复
              }

              this.messages.push(aiReply)
              this.$nextTick(() => {
                this.scrollToBottom()
              })
            }

            // 通过WebSocket通知对方
            if (this.socket && this.isConnected) {
              this.socket.send(JSON.stringify({
                type: 'CHAT_MESSAGE',
                data: {
                  id: replyRes.data.id,
                  content: replyRes.data.content,
                  createTime: replyRes.data.createTime,
                  fromUserId: this.currentUser.id,
                  toUserId: requestData.fromUserId,
                  isAI: true
                }
              }))
            }

            this.$message.success('AI代回复成功')
            console.log('AI代回复成功')
          } else {
            console.error('发送AI回复失败:', replyRes.msg)
            this.$message.error('发送AI回复失败: ' + (replyRes.msg || '未知错误'))
          }
        } else {
          console.error('AI生成回复失败:', aiRes.msg)
          this.$message.error('AI生成回复失败: ' + (aiRes.msg || '未知错误'))
        }
      } catch (error) {
        console.error('处理AI代回复请求失败:', error)
        this.$message.error('处理AI代回复请求失败: ' + error.message)
      }
    },

    // 头像加载失败处理
    handleAvatarError(event) {
      console.log('头像加载失败，使用默认头像')
      event.target.src = this.getDefaultAvatar()
      event.target.onerror = null // 防止无限循环
    },

    // 根据用户ID获取好友名称
    getFriendName(userId) {
      console.log('获取好友名称，userId:', userId, '好友列表:', this.friends)
      const friend = this.friends.find(f => f.friendId === userId)
      if (friend) {
        const name = friend.displayName || friend.friendName || friend.userName || friend.realName || '好友'
        console.log('找到好友，名称:', name)
        return name
      }
      console.log('未找到好友，返回默认名称')
      return '好友'
    },

    // 启动定时轮询（作为WebSocket的备用方案）
    startPolling() {
      // 每5秒检查一次新消息
      this.pollingTimer = setInterval(() => {
        if (this.activeFriend && this.isConnected) {
          this.checkNewMessages(this.activeFriend.friendId)
        }
      }, 5000)

      console.log('定时轮询已启动，间隔: 5秒')
    },

    // 停止定时轮询
    stopPolling() {
      if (this.pollingTimer) {
        clearInterval(this.pollingTimer)
        this.pollingTimer = null
        console.log('定时轮询已停止')
      }
    },

    // 检查新消息
    async checkNewMessages(friendId) {
      try {
        const res = await getChatHistory(friendId, 10)
        if (res.code && res.data && res.data.length > 0) {
          // 获取最新消息
          const latestMessage = res.data[0]
          const lastCheckTime = this.lastMessageCheckTime[friendId] || 0

          // 如果有更新且不是自己的消息
          if (new Date(latestMessage.createTime).getTime() > lastCheckTime &&
              latestMessage.fromUserId !== this.currentUser.id) {
            // 检查消息是否已经在列表中
            const exists = this.messages.some(msg => msg.id === latestMessage.id)
            if (!exists) {
              console.log('通过轮询发现新消息:', latestMessage)

              this.messages.push({
                id: latestMessage.id,
                content: latestMessage.content,
                createTime: latestMessage.createTime,
                isOwn: false,
                fromUserId: latestMessage.fromUserId,
                toUserId: latestMessage.toUserId
              })

              // 更新时间戳
              this.lastMessageCheckTime[friendId] = new Date().getTime()

              // 滚动到底部
              this.$nextTick(() => {
                this.scrollToBottom()
              })
            }
          }
        }
      } catch (error) {
        console.error('轮询检查新消息失败:', error)
      }
    },

    // 格式化头像URL
    formatAvatarUrl(avatarPath) {
      // 检查缓存
      if (this.avatarCache.has(avatarPath)) {
        return this.avatarCache.get(avatarPath)
      }

      console.log('原始头像路径:', avatarPath)

      let finalUrl

      if (!avatarPath || avatarPath === 'null' || avatarPath === 'undefined') {
        finalUrl = this.getDefaultAvatar()
      } else if (avatarPath.startsWith('http') || avatarPath.startsWith('/uploads/') || avatarPath.startsWith('/')) {
        // 如果已经是完整URL，直接返回
        finalUrl = avatarPath
      } else {
        // 添加uploads路径前缀
        finalUrl = `/uploads/${avatarPath}`
      }

      console.log('格式化后头像路径:', finalUrl)

      // 缓存结果
      this.avatarCache.set(avatarPath, finalUrl)
      return finalUrl
    },

    // 获取默认头像 - 使用base64编码的SVG头像
    getDefaultAvatar() {
      return 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPGNpcmNsZSBjeD0iMjAiIGN5PSIyMCIgcj0iMjAiIGZpbGw9IiNlNGU3ZWQiLz4KPHBhdGggZD0iTTIwIDIyQzIyLjIwOTEgMjIgMjQgMjAuMjA5MSAyNCAxOEMyNCAxNS43OTA5IDIyLjIwOTEgMTQgMjAgMTRDMTcuNzkwOSAxNCAxNiAxNS43OTA5IDE2IDE4QzE2IDIwLjIwOTEgMTcuNzkwOSAyMiAyMCAyMloiIGZpbGw9IiM5MDkzOTkiLz4KPHBhdGggZD0iTTI2IDI2QzI2IDI4LjIwOTEgMjMuMzEzNyAzMCAyMCAzMEMxNi42ODYzIDMwIDE0IDI4LjIwOTEgMTQgMjZDMjQgMjYgMjYgMjYgMjYgMjZaIiBmaWxsPSIjOTA5Mzk5Ii8+Cjwvc3ZnPgo='
    },

    // 搜索用户
    async searchUser() {
      if (!this.searchUserName.trim()) {
        this.$message.warning('请输入搜索内容')
        return
      }

      try {
        const res = await searchUser(this.searchUserName)
        if (res.code) {
          this.searchResult = res.data
        } else {
          this.$message.error(res.msg || '未找到用户')
          this.searchResult = null
        }
      } catch (error) {
        console.error('搜索用户失败:', error)
        this.$message.error('搜索失败')
      }
    },

    // 加载待处理请求
    async loadPendingRequests() {
      try {
        const res = await getPendingRequests()
        if (res.code) {
          this.pendingRequests = res.data || []
          console.log('待处理请求:', this.pendingRequests)
        } else {
          console.warn('获取待处理请求失败:', res.msg)
          // 不显示错误提示，可能是没有待处理请求
          this.pendingRequests = []
        }
      } catch (error) {
        console.error('加载待处理请求异常:', error)
        // 网络错误或服务器错误时，静默处理，不影响主要功能
        this.pendingRequests = []
      }
    },

    // 处理好友请求 (同意/拒绝)
    async handleRequest(requestId, status) {
      try {
        const res = await handleFriendRequest(requestId, status)

        if (res.code) {
          this.$message.success(status === 1 ? '已同意' : '已拒绝')
          // 从列表中移除
          this.pendingRequests = this.pendingRequests.filter(req => req.id !== requestId)
          if (status === 1) {
            await this.loadFriends()
          }
        } else {
          this.$message.error(res.msg || '操作失败')
        }
      } catch (error) {
        console.error('处理好友请求失败:', error)
        this.$message.error('操作失败')
      }
    },

    // 发送好友请求
    async sendFriendRequest() {
      if (!this.searchResult) return

      try {
        const res = await sendFriendRequest(this.searchResult.id)
        if (res.code) {
          this.$message.success(res.msg || '请求已发送')
          this.showAddFriendDialog = false
          this.searchResult = null
          this.searchUserName = ''
          // 重新加载待处理请求
          await this.loadPendingRequests()
        } else {
          this.$message.error(res.msg || '发送失败')
        }
      } catch (error) {
        console.error('发送好友请求失败:', error)
        this.$message.error('发送失败')
      }
    },

    // 删除好友
    async deleteFriend(friend) {
      try {
        await this.$confirm('确定要删除该好友吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        console.log('删除好友，friendId:', friend.friendId)
        const res = await deleteFriend(friend.friendId)
        console.log('删除好友API响应:', res)

        if (res.code) {
          this.$message.success('删除成功')

          // 立即从好友列表中移除该好友
          const friendIndex = this.friends.findIndex(f => f.friendId === friend.friendId)
          console.log('找到好友索引:', friendIndex)

          if (friendIndex !== -1) {
            this.friends.splice(friendIndex, 1)
            console.log('已从列表中移除好友')
          }

          // 如果删除的是当前选中的好友，清空聊天
          if (this.activeFriend && this.activeFriend.friendId === friend.friendId) {
            this.activeFriend = null
            this.messages = []
            console.log('已清空当前聊天')
          }

          // 重新加载好友列表以确保数据一致性
          await this.loadFriends()
          console.log('已重新加载好友列表')
        } else {
          this.$message.error(res.msg || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除好友失败:', error)
          this.$message.error('删除失败')
        }
      }
    },

    // 显示修改备注对话框
    showEditRemarkDialog() {
      if (!this.activeFriend) return

      this.currentEditFriend = this.activeFriend
      this.remarkForm.remark = this.activeFriend.remark || ''
      this.showEditRemarkDialogVisible = true
    },

    // 提交修改备注
    async submitEditRemark() {
      if (!this.currentEditFriend) return

      try {
        const res = await updateFriendRemark(
          this.currentEditFriend.friendId,
          this.remarkForm.remark
        )

        if (res.code) {
          this.$message.success('修改备注成功')
          this.showEditRemarkDialogVisible = false

          // 重新加载好友列表以更新显示
          await this.loadFriends()

          // 如果当前正在与该好友聊天，更新activeFriend
          if (this.activeFriend && this.activeFriend.friendId === this.currentEditFriend.friendId) {
            const updatedFriend = this.friends.find(f => f.friendId === this.currentEditFriend.friendId)
            if (updatedFriend) {
              this.activeFriend = { ...updatedFriend }
            }
          }
        } else {
          this.$message.error(res.msg || '修改备注失败')
        }
      } catch (error) {
        console.error('修改备注失败:', error)
        this.$message.error('修改备注失败')
      }
    },

    // 格式化时间
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date

      if (diff < 60000) {
        return '刚刚'
      } else if (diff < 3600000) {
        return `${Math.floor(diff / 60000)}分钟前`
      } else if (diff < 86600000) {
        return `${Math.floor(diff / 3600000)}小时前`
      } else {
        return `${date.getMonth() + 1}-${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
      }
    },

    // 获取角色样式类
    getRoleClass(role) {
      const roleMap = {
        1: 'role-student',
        2: 'role-teacher',
        3: 'role-admin'
      }
      return roleMap[role] || 'role-default'
    },

    // 获取角色文本
    getRoleText(role) {
      const roleMap = {
        1: '学生',
        2: '教师',
        3: '管理员'
      }
      return roleMap[role] || '未知'
    }
  }
}
</script>

<style lang="scss" scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 120px);
  background: #f5f7fa;

  // 左侧好友列表
  .friend-sidebar {
    width: 280px;
    background: white;
    border-right: 1px solid #e4e7ed;
    display: flex;
    flex-direction: column;

    .sidebar-header {
      padding: 16px 20px;
      border-bottom: 1px solid #e4e7ed;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }

      .header-buttons {
        display: flex;
        gap: 8px;

        ::v-deep .el-button {
          padding: 8px;
          font-size: 18px;

          &:hover {
            color: #409EFF;
          }
        }

        // 在线状态按钮样式
        .online-btn {
          color: #67c23a !important;

          &:hover {
            color: #85ce61 !important;
          }
        }

        .offline-btn {
          color: #909399 !important;

          &:hover {
            color: #a6a9ad !important;
          }
        }

        // AI代理按钮样式
        .ai-agent-btn {
          color: #909399 !important;

          &:hover {
            color: #409EFF !important;
          }

          &.ai-agent-active {
            color: #e6a23c !important;

            &:hover {
              color: #ebb563 !important;
            }
          }
        }
      }
    }

    .search-box {
      padding: 12px 16px;
      border-bottom: 1px solid #e4e7ed;
    }

    .friend-list {
      flex: 1;
      overflow-y: auto;

      .friend-item {
        display: flex;
        align-items: center;
        padding: 12px 16px;
        cursor: pointer;
        transition: all 0.3s;
        border-bottom: 1px solid #f0f0f0;

        &:hover {
          background: #f5f7fa;
        }

        &.active {
          background: #ecf5ff;
          border-left: 3px solid #409EFF;
        }

        .friend-avatar-wrapper {
          position: relative;
          margin-right: 12px;

          .friend-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            object-fit: cover;
          }
        }

        .friend-info {
          flex: 1;
          min-width: 0;

          .friend-name {
            font-size: 14px;
            font-weight: 500;
            color: #303133;
            margin-bottom: 4px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .last-message {
            font-size: 12px;
            color: #909399;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }

      .empty-friends {
        text-align: center;
        padding: 60px 20px;
        color: #909399;

        i {
          font-size: 48px;
          margin-bottom: 12px;
          display: block;
        }

        p {
          font-size: 14px;
          margin: 0;
        }
      }
    }
  }

  // 右侧聊天区域
  .chat-main {
    flex: 1;
    background: white;
    display: flex;
    flex-direction: column;

    .chat-window {
      display: flex;
      flex-direction: column;
      height: 100%;
      box-shadow: 0 0 20px rgba(0, 0, 0, 0.05);

      .chat-header {
        padding: 20px 24px;
        border-bottom: 1px solid #e4e7ed;
        display: flex;
        justify-content: space-between;
        align-items: center;
        background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

        .chat-user {
          display: flex;
          align-items: center;
          gap: 16px;

          .user-info {
            .user-name {
              font-size: 18px;
              font-weight: 600;
              color: #303133;
              letter-spacing: 0.5px;
            }
          }
        }

        .header-actions {
          display: flex;
          gap: 8px;

          .action-btn {
            padding: 8px 12px;
            font-size: 18px;
            border-radius: 8px;
            transition: all 0.3s ease;

            &:hover {
              background: rgba(102, 126, 234, 0.1);
              color: #667eea;
            }

            &.delete-btn {
              &:hover {
                background: rgba(245, 108, 108, 0.1);
                color: #f56c6c;
              }
            }
          }
        }
      }

      .messages-area {
        flex: 1;
        overflow-y: auto;
        padding: 20px;
        background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);

        .message-item {
          display: flex;
          margin-bottom: 16px;
          animation: messageSlideIn 0.3s ease-out;

          @keyframes messageSlideIn {
            from {
              opacity: 0;
              transform: translateY(10px);
            }
            to {
              opacity: 1;
              transform: translateY(0);
            }
          }

          &.own {
            justify-content: flex-end;

            .msg-content {
              align-items: flex-end;

              .msg-bubble {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                border-radius: 18px 18px 4px 18px;
                box-shadow: 0 2px 8px rgba(102, 126, 234, 0.25);

                &:hover {
                  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.35);
                  transform: translateY(-1px);
                }
              }
            }
          }

          &.other {
            justify-content: flex-start;

            .msg-content {
              align-items: flex-start;

              .msg-bubble {
                background: white;
                color: #303133;
                border-radius: 18px 18px 18px 4px;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
                border: 1px solid #e4e7ed;

                &:hover {
                  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
                  transform: translateY(-1px);
                }
              }
            }
          }

          .msg-content {
            display: flex;
            flex-direction: column;
            max-width: 65%;
            transition: all 0.3s ease;

            .msg-bubble {
              padding: 12px 16px;
              font-size: 14px;
              line-height: 1.6;
              word-wrap: break-word;
              word-break: break-word;
              transition: all 0.3s ease;
            }

            .msg-time {
              font-size: 11px;
              color: #909399;
              margin-top: 6px;
              padding: 0 4px;
              opacity: 0.8;
            }
          }
        }

        .empty-messages {
          text-align: center;
          padding: 100px 20px;
          color: #909399;

          i {
            font-size: 72px;
            margin-bottom: 20px;
            display: block;
            opacity: 0.5;
            animation: float 3s ease-in-out infinite;

            @keyframes float {
              0%, 100% {
                transform: translateY(0);
              }
              50% {
                transform: translateY(-10px);
              }
            }
          }

          p {
            font-size: 15px;
            margin: 0;
            letter-spacing: 0.5px;
          }
        }
      }

      .input-area {
        padding: 20px 24px;
        border-top: 1px solid #e4e7ed;
        background: white;
        box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);

        ::v-deep .el-textarea__inner {
          border: 2px solid #e4e7ed;
          border-radius: 12px;
          resize: none;
          padding: 12px 16px;
          font-size: 14px;
          line-height: 1.6;
          transition: all 0.3s ease;

          &:hover {
            border-color: #c0c4cc;
          }

          &:focus {
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
          }

          &::placeholder {
            color: #c0c4cc;
          }
        }

        .input-actions {
          display: flex;
          justify-content: flex-end;
          margin-top: 16px;

          ::v-deep .el-button {
            padding: 10px 24px;
            border-radius: 8px;
            font-weight: 500;
            letter-spacing: 0.5px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            transition: all 0.3s ease;

            &:hover:not(:disabled) {
              transform: translateY(-2px);
              box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
            }

            &:active:not(:disabled) {
              transform: translateY(0);
            }

            &:disabled {
              opacity: 0.5;
              cursor: not-allowed;
            }
          }
        }
      }
    }

    .empty-chat {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #909399;
      background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);

      i {
        font-size: 96px;
        margin-bottom: 24px;
        opacity: 0.4;
        animation: pulse 2s ease-in-out infinite;

        @keyframes pulse {
          0%, 100% {
            transform: scale(1);
            opacity: 0.4;
          }
          50% {
            transform: scale(1.05);
            opacity: 0.6;
          }
        }
      }

      p {
        font-size: 18px;
        margin: 0;
        letter-spacing: 1px;
        font-weight: 500;
      }
    }
  }
}

// 对话框样式
::v-deep .add-friend-dialog,
::v-deep .pending-dialog {
  .el-dialog__body {
    padding: 20px;
  }

  .dialog-content {
    .search-btn {
      margin-top: 12px;
      width: 100%;
    }

    .search-result {
      margin-top: 20px;
      display: flex;
      align-items: center;
      padding: 16px;
      background: #f5f7fa;
      border-radius: 8px;
      gap: 12px;

      .result-avatar {
        width: 48px;
        height: 48px;
        border-radius: 50%;
      }

      .result-info {
        flex: 1;

        .result-name {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }

        .result-username {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }

  .empty-state {
    text-align: center;
    padding: 40px 20px;
    color: #909399;

    i {
      font-size: 64px;
      margin-bottom: 16px;
      display: block;
    }

    p {
      font-size: 14px;
      margin: 0;
    }
  }

  .request-list {
    .request-item {
      display: flex;
      align-items: center;
      padding: 12px;
      border-bottom: 1px solid #e4e7ed;
      gap: 12px;

      &:last-child {
        border-bottom: none;
      }

      .req-avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
      }

      .req-info {
        flex: 1;

        .req-name {
          font-size: 14px;
          font-weight: 500;
          color: #303133;
          margin-bottom: 4px;
        }

        .req-time {
          font-size: 12px;
          color: #909399;
        }
      }

      .req-actions {
        display: flex;
        gap: 8px;
      }
    }
  }
}

// 滚动条样式
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #c0c4cc 0%, #dcdfe6 100%);
  border-radius: 4px;

  &:hover {
    background: linear-gradient(135deg, #a6a9ad 0%, #c0c4cc 100%);
  }
}

::-webkit-scrollbar-track {
  background: transparent;
}

// 修改备注对话框样式
.edit-remark-dialog {
  ::v-deep .el-dialog__header {
    border-bottom: 1px solid #e4e7ed;
    padding: 20px;
  }

  ::v-deep .el-dialog__body {
    padding: 30px 20px;
  }

  ::v-deep .el-form-item__label {
    font-weight: 500;
  }
}

// AI助手按钮样式
.ai-btn {
  &:hover {
    background: rgba(102, 126, 234, 0.1);
    color: #667eea;
  }

  &.is-loading {
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// AI消息样式
.msg-bubble {
  &.ai-message {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    position: relative;
    border: 2px solid rgba(255, 255, 255, 0.3);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

    .ai-badge {
      display: inline-block;
      background: rgba(255, 255, 255, 0.25);
      padding: 2px 8px;
      border-radius: 12px;
      font-size: 12px;
      margin-right: 8px;
      margin-bottom: 4px;
      font-weight: 500;
      backdrop-filter: blur(10px);
    }
  }
}
</style>
