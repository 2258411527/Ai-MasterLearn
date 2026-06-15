import request from '@/utils/request'

export function aiChat(data) {
  return request({
    url: 'ai/chat',
    method: 'post',
    data
  })
}
