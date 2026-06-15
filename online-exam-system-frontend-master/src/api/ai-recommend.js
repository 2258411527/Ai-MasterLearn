import request from '@/utils/request'

export function getAIRecommendations() {
  return request({
    url: '/api/ai-recommend/personalized',
    method: 'get'
  })
}

export function getDailyStudyPlan() {
  return request({
    url: '/api/ai-recommend/daily-plan',
    method: 'get'
  })
}

export function getWeakAnalysis() {
  return request({
    url: '/api/ai-recommend/weak-analysis',
    method: 'get'
  })
}

export function getSubjectAdvice(subject) {
  return request({
    url: `/api/ai-recommend/advice/${subject}`,
    method: 'get'
  })
}
