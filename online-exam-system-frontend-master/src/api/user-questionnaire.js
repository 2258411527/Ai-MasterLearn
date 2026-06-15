import request from '@/utils/request'

// 检查用户是否已完成问卷
export function checkQuestionnaireCompleted() {
  return request({
    url: '/user-questionnaire/check-completed',
    method: 'get'
  })
}

// 提交用户问卷
export function submitQuestionnaire(data) {
  return request({
    url: '/user-questionnaire/submit',
    method: 'post',
    data
  })
}

// 获取用户学习仪表盘数据
export function getStudyDashboard() {
  return request({
    url: '/user-questionnaire/dashboard',
    method: 'get'
  })
}

// 生成AI个性化推荐
export function generateAiRecommendations() {
  return request({
    url: '/user-questionnaire/generate-recommendations',
    method: 'post'
  })
}

// 获取用户画像数据
export function getUserProfile() {
  return request({
    url: '/user-questionnaire/dashboard',
    method: 'get'
  })
}

// 获取用户学习分析数据
export function getUserStudyAnalysis() {
  return request({
    url: '/ai-learning/learning-data',
    method: 'get'
  })
}

// 获取AI推荐内容
export function getAiRecommendations() {
  return request({
    url: '/ai-learning/recommendations',
    method: 'get'
  })
}

// 获取AI消息
export function getAIMessage() {
  return request({
    url: '/ai-learning/ai-message',
    method: 'get'
  })
}

// 更新用户学习行为
export function updateUserLearningBehavior(data) {
  return request({
    url: '/ai-learning/learning-behavior',
    method: 'post',
    data
  })
}