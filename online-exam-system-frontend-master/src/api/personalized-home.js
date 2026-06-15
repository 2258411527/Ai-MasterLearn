import request from '@/utils/request'

export function getPersonalizedHome() {
  return request({
    url: '/api/home/personalized',
    method: 'get'
  })
}

export function refreshHomeCache() {
  return request({
    url: '/api/home/refresh',
    method: 'post'
  })
}

export function clearHomeCache() {
  return request({
    url: '/api/home/cache',
    method: 'delete'
  })
}
