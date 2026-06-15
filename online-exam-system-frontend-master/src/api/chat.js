import request from '@/utils/request'

// 好友关系相关API
export function getFriendList() {
  return request({
    url: 'friend/list',
    method: 'get'
  })
}

export function searchUser(keyword) {
  return request({
    url: 'friend/search',
    method: 'get',
    params: { keyword }
  })
}

export function sendFriendRequest(friendId) {
  return request({
    url: `friend/request/${friendId}`,
    method: 'post'
  })
}

export function handleFriendRequest(requestId, status) {
  return request({
    url: `friend/request/${requestId}/handle`,
    method: 'post',
    params: { status }
  })
}

export function deleteFriend(friendId) {
  return request({
    url: `friend/${friendId}`,
    method: 'delete'
  })
}

export function updateFriendRemark(friendId, remark) {
  return request({
    url: `friend/${friendId}/remark`,
    method: 'put',
    params: { remark }
  })
}

export function getPendingRequests() {
  return request({
    url: 'friend/requests/pending',
    method: 'get'
  })
}

// 聊天消息相关API
export function sendMessage(data) {
  return request({
    url: 'chat/send',
    method: 'post',
    data
  })
}

export function getChatHistory(friendId, limit = 50) {
  return request({
    url: `chat/history/${friendId}`,
    method: 'get',
    params: { limit }
  })
}

export function markMessagesAsRead(friendId) {
  return request({
    url: `chat/read/${friendId}`,
    method: 'post'
  })
}

export function getUnreadMessageCount() {
  return request({
    url: 'chat/unread/count',
    method: 'get'
  })
}

export function deleteChatHistory(friendId) {
  return request({
    url: `chat/history/${friendId}`,
    method: 'delete'
  })
}
