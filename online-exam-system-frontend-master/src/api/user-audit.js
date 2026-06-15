import request from '@/utils/request'

// 获取待审核用户列表
export function getPendingUsers(params) {
  return request({
    url: '/user-audit/pending',
    method: 'get',
    params
  })
}

// 审核通过用户
export function approveUser(userId, data) {
  return request({
    url: `/user-audit/approve/${userId}`,
    method: 'post',
    data
  })
}

// 审核拒绝用户
export function rejectUser(userId, data) {
  return request({
    url: `/user-audit/reject/${userId}`,
    method: 'post',
    data
  })
}

// 获取所有审核记录
export function getAuditRecords(params) {
  return request({
    url: '/user-audit/records',
    method: 'get',
    params
  })
}
