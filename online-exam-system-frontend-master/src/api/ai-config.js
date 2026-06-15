import request from '@/utils/request'

export function aiConfigPaging(params) {
  return request({
    url: 'ai-config/paging',
    method: 'get',
    params
  })
}

export function aiConfigAdd(data) {
  return request({
    url: 'ai-config',
    method: 'post',
    data
  })
}

export function aiConfigUpdate(id, data) {
  return request({
    url: 'ai-config/' + id,
    method: 'put',
    data
  })
}

export function aiConfigDel(id) {
  return request({
    url: 'ai-config/' + id,
    method: 'delete'
  })
}

export function aiConfigActivate(id) {
  return request({
    url: 'ai-config/activate/' + id,
    method: 'put'
  })
}

export function aiConfigGetActive() {
  return request({
    url: 'ai-config/active',
    method: 'get'
  })
}

export function aiConfigGetModels() {
  return request({
    url: 'ai-config/models',
    method: 'get'
  })
}

export function aiConfigGetById(id) {
  return request({
    url: 'ai-config/' + id,
    method: 'get'
  })
}
