import request from '@/utils/request'

/**
 * AI阅卷单个题目（教师/管理员）
 */
export function aiGradeSingleQuestion(data) {
  return request({
    url: '/ai-grading/grade-single',
    method: 'post',
    data
  })
}

/**
 * AI解析单个题目（学生端，仅分析不保存分数）
 */
export function aiAnalyzeQuestion(data) {
  return request({
    url: '/ai-grading/analyze-question',
    method: 'post',
    data
  })
}

/**
 * AI阅卷整张试卷（教师/管理员）
 */
export function aiGradeEntireExam(examId) {
  return request({
    url: `/ai-grading/grade-exam/${examId}`,
    method: 'get'
  })
}

/**
 * 获取AI阅卷历史记录
 */
export function getAiGradingHistory(examId) {
  return request({
    url: `/ai-grading/history/${examId}`,
    method: 'get'
  })
}
