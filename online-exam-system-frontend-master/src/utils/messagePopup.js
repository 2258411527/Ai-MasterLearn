import Vue from 'vue'
import MessagePopupComponent from '@/components/MessagePopup/index.vue'

const MessagePopup = Vue.extend(MessagePopupComponent)

let instance = null
let messageQueue = []
let isShowing = false

function createInstance() {
  if (!instance) {
    instance = new MessagePopup({
      el: document.createElement('div')
    })
    document.body.appendChild(instance.$el)
    
    instance.$on('close', (msg) => {
      isShowing = false
      if (msg && msg.id) {
        markMessageRead(msg.id)
      }
      showNext()
    })
    
    instance.$on('action', (msg) => {
      isShowing = false
      handleAction(msg)
      showNext()
    })
    
    instance.$on('ignore', (msg) => {
      isShowing = false
      showNext()
    })
  }
  return instance
}

function markMessageRead(id) {
  const { markAsRead } = require('@/api/notification')
  markAsRead(id).catch(() => {})
}

function handleAction(msg) {
  const router = require('@/router').default
  switch (msg.type) {
    case 'token_low':
      router.push('/token')
      break
    case 'friend_request':
    case 'friend_message':
      router.push('/chat')
      break
    default:
      router.push('/notifications')
  }
}

function showNext() {
  if (messageQueue.length > 0 && !isShowing) {
    const msg = messageQueue.shift()
    show(msg)
  }
}

function show(message) {
  const inst = createInstance()
  isShowing = true
  inst.message = message
  inst.show()
}

function push(message, immediate = false) {
  if (immediate && !isShowing) {
    show(message)
  } else {
    messageQueue.push(message)
    if (!isShowing) {
      showNext()
    }
  }
}

function pushBatch(messages) {
  messageQueue = messageQueue.concat(messages)
  if (!isShowing) {
    showNext()
  }
}

function clear() {
  messageQueue = []
}

export default {
  show,
  push,
  pushBatch,
  clear,
  getQueueLength: () => messageQueue.length
}
