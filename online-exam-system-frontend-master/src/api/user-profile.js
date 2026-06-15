import request from '@/utils/request'

export function submitQuestionnaire(data) {
  return request({
    url: '/user-profile/submit',
    method: 'post',
    data
  })
}

export function getLearningPlan() {
  return request({
    url: '/user-profile/plan',
    method: 'get'
  })
}
