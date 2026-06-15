import request from '@/utils/request'

export function getAlipayConfigPage(params) {
  return request({
    url: '/alipay-config/paging',
    method: 'get',
    params
  })
}

export function addAlipayConfig(data) {
  return request({
    url: '/alipay-config',
    method: 'post',
    data
  })
}

export function updateAlipayConfig(id, data) {
  return request({
    url: `/alipay-config/${id}`,
    method: 'put',
    data
  })
}

export function deleteAlipayConfig(id) {
  return request({
    url: `/alipay-config/${id}`,
    method: 'delete'
  })
}

export function activateAlipayConfig(id) {
  return request({
    url: `/alipay-config/activate/${id}`,
    method: 'put'
  })
}
