import request from '@/utils/request'

/**
 * 获取考试的所有AI阅卷结果
 */
export function getExamAiGradingResults(examId, userId) {
  return request({
    url: `/ai-grading-result/exam/${examId}/user/${userId}`,
    method: 'get'
  })
}

/**
 * 获取单个题目的AI阅卷结果
 */
export function getQuestionAiGradingResult(questionId, examId, userId) {
  return request({
    url: `/ai-grading-result/question/${questionId}/exam/${examId}/user/${userId}`,
    method: 'get'
  })
}

/**
 * 获取用户的AI阅卷历史
 */
export function getUserAiGradingHistory(userId) {
  return request({
    url: `/ai-grading-result/user/${userId}/history`,
    method: 'get'
  })
}

/**
 * 获取考试AI阅卷状态
 */
export function getExamAiGradingStatus(examId, userId) {
  return request({
    url: `/ai-grading-result/status/exam/${examId}/user/${userId}`,
    method: 'get'
  })
}
