import request from '@/utils/request'

export function getAIRecommendQuestions(limit = 4) {
  return request({
    url: '/api/ai-question/recommend',
    method: 'get',
    params: { limit }
  })
}

export function getQuestionsByKnowledge(knowledgePoint, limit = 4) {
  return request({
    url: '/api/ai-question/by-knowledge',
    method: 'get',
    params: { knowledgePoint, limit }
  })
}

export function getWeakPointQuestions(limit = 4) {
  return request({
    url: '/api/ai-question/weak-points',
    method: 'get',
    params: { limit }
  })
}

export function getRecommendReason(questionId) {
  return request({
    url: `/api/ai-question/reason/${questionId}`,
    method: 'get'
  })
}
