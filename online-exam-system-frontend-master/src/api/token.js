import request from '@/utils/request'

export function getTokenBalance() {
  return request({
    url: '/token/balance',
    method: 'get'
  })
}

export function purchaseToken(data) {
  return request({
    url: '/token/purchase',
    method: 'post',
    data
  })
}

export function getTokenPackages() {
  return request({
    url: '/token/packages',
    method: 'get'
  })
}

export function getTokenTransactions(params) {
  return request({
    url: '/token/transactions',
    method: 'get',
    params
  })
}

export function createAlipayPayment(packageId) {
  return request({
    url: '/alipay/pay',
    method: 'post',
    params: { packageId }
  })
}

export function queryAlipayOrder(orderNo) {
  return request({
    url: '/alipay/query',
    method: 'get',
    params: { orderNo }
  })
}

export function adminGetPackages(params) {
  return request({
    url: '/token/admin/packages',
    method: 'get',
    params
  })
}

export function adminAddPackage(data) {
  return request({
    url: '/token/admin/packages',
    method: 'post',
    data
  })
}

export function adminUpdatePackage(id, data) {
  return request({
    url: `/token/admin/packages/${id}`,
    method: 'put',
    data
  })
}

export function adminDeletePackage(id) {
  return request({
    url: `/token/admin/packages/${id}`,
    method: 'delete'
  })
}

export function adminGetUserTokens(params) {
  return request({
    url: '/token/admin/users',
    method: 'get',
    params
  })
}

export function adminAdjustToken(data) {
  return request({
    url: '/token/admin/adjust',
    method: 'post',
    data
  })
}
