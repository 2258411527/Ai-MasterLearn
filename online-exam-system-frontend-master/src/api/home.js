import request from '@/utils/request'

// 获取首页数据
export function getHomePageData(userId) {
  return request({
    url: '/api/home/data',
    method: 'get',
    params: { userId }
  })
}

// 获取学习时长趋势
export function getStudyDurationTrend(userId) {
  return request({
    url: '/api/home/study-trend',
    method: 'get',
    params: { userId }
  })
}

// 添加任务
export function addTask(userId, form) {
  return request({
    url: '/api/home/task',
    method: 'post',
    params: { userId },
    data: form
  })
}

// AI生成任务
export function generateTasksByAI(userId) {
  return request({
    url: '/api/home/task/ai-generate',
    method: 'post',
    params: { userId }
  })
}

// 完成任务
export function completeTask(userId, taskId) {
  return request({
    url: `/api/home/task/${taskId}/complete`,
    method: 'put',
    params: { userId }
  })
}

// 添加倒计时
export function addCountdown(userId, form) {
  return request({
    url: '/api/home/countdown',
    method: 'post',
    params: { userId },
    data: form
  })
}

// 获取倒计时列表
export function getCountdowns(userId) {
  return request({
    url: '/api/home/countdowns',
    method: 'get',
    params: { userId }
  })
}

// 删除倒计时
export function deleteCountdown(userId, countdownId) {
  return request({
    url: `/api/home/countdown/${countdownId}`,
    method: 'delete',
    params: { userId }
  })
}

// 获取每日一笑
export function getDailyJoke(userId) {
  return request({
    url: '/api/home/daily-joke',
    method: 'get',
    params: { userId }
  })
}

// 获取错题分析
export function getWrongAnalysis(userId) {
  return request({
    url: '/api/home/wrong-analysis',
    method: 'get',
    params: { userId }
  })
}