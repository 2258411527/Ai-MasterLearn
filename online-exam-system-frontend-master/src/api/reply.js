
import request from '@/utils/request'

export function replyAdd(data) {
  return request({
    url: 'reply/add',
    method: 'post',
    data
  })
}

export function replyDel(id) {
  return request({
    url: `reply/delete/${id}`,
    method: 'delete'
  })
}

export function replyquery(orderBy, id) {
  return request({
    url: `reply/query/${orderBy}/${id}`,
    method: 'get'
  })
}

