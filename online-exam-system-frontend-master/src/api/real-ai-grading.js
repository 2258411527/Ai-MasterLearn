import request from '@/utils/request'

/**
 * 真实AI阅卷 - 单题评分
 */
export function realAiGradeSingleQuestion(data) {
  return request({
    url: '/real-ai-grading/grade-single',
    method: 'post',
    data
  })
}

/**
 * 真实AI阅卷 - 批量阅卷
 */
export function realAiGradeBatchQuestions(examId) {
  return request({
    url: `/real-ai-grading/grade-batch/${examId}`,
    method: 'post'
  })
}

/**
 * 获取阅卷进度
 */
export function getRealAiGradingProgress(examId) {
  return request({
    url: `/real-ai-grading/progress/${examId}`,
    method: 'get'
  })
}

/**
 * 获取可阅卷的试卷列表
 */
export function getGradableExams() {
  return request({
    url: '/real-ai-grading/gradable-exams',
    method: 'get'
  })
}

/**
 * 检查AI阅卷服务状态
 */
export function getAiGradingStatus() {
  return request({
    url: '/real-ai-grading/status',
    method: 'get'
  })
}
