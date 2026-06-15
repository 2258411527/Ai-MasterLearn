import request from '@/utils/request'

/**
 * 获取聊天记录
 */
export function aiChatHistory() {
  return request({
    url: 'ai-chat-history',
    method: 'get'
  })
}

/**
 * 分页获取聊天记录
 */
export function aiChatHistoryPaging(params) {
  return request({
    url: 'ai-chat-history/paging',
    method: 'get',
    params
  })
}

/**
 * 清除聊天记录
 */
export function aiChatHistoryClear() {
  return request({
    url: 'ai-chat-history',
    method: 'delete'
  })
}
