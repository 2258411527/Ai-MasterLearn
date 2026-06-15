import request from '@/utils/request'

export function getTokenConsumeConfigs() {
  return request({
    url: '/api/token-consume-config/list',
    method: 'get'
  })
}

export function getTokenConsumeConfigMap() {
  return request({
    url: '/api/token-consume-config/map',
    method: 'get'
  })
}

export function updateTokenConsumeConfig(data) {
  return request({
    url: '/api/token-consume-config/update',
    method: 'put',
    data
  })
}

export function updateTokenConsumeCost(key, cost) {
  return request({
    url: `/api/token-consume-config/update/${key}/${cost}`,
    method: 'put'
  })
}

export function initTokenConsumeConfigs() {
  return request({
    url: '/api/token-consume-config/init',
    method: 'post'
  })
}

export function toggleTokenConsumeConfig(id) {
  return request({
    url: `/api/token-consume-config/toggle/${id}`,
    method: 'put'
  })
}
