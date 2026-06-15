import request from '@/utils/request'

export function getNotifications(params) {
  return request({
    url: 'notifications/list',
    method: 'get',
    params
  })
}

export function getUnreadCount() {
  return request({
    url: 'notifications/unread-count',
    method: 'get'
  })
}

export function getMustReadCount() {
  return request({
    url: 'notifications/must-read-count',
    method: 'get'
  })
}

export function markAsRead(id) {
  return request({
    url: `notifications/read/${id}`,
    method: 'put'
  })
}

export function markAllAsRead() {
  return request({
    url: 'notifications/read-all',
    method: 'put'
  })
}

export function broadcastNotification(data) {
  return request({
    url: 'notifications/broadcast',
    method: 'post',
    params: data
  })
}

export function getUnreadSummary() {
  return request({
    url: 'notifications/unread-summary',
    method: 'get'
  })
}

export function getUnreadCountByType(type) {
  return request({
    url: `notifications/unread-count/${type}`,
    method: 'get'
  })
}

export function markAllAsReadByType(type) {
  return request({
    url: `notifications/read-all/${type}`,
    method: 'put'
  })
}

export function sendTokenLowNotification(userId, remaining) {
  return request({
    url: 'notifications/token-low',
    method: 'post',
    params: { userId, remaining }
  })
}

export function sendSystemUpgradeNotification(data) {
  return request({
    url: 'notifications/system-upgrade',
    method: 'post',
    params: data
  })
}

export function sendFriendMessageNotification(data) {
  return request({
    url: 'notifications/friend-message',
    method: 'post',
    params: data
  })
}
