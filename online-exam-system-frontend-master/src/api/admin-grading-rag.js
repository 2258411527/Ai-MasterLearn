import request from '@/utils/request'

export function parseAdminGradingMaterial(materialId) {
  return request({
    url: 'rag/admin-grading/parse/' + materialId,
    method: 'post',
    timeout: 30000
  })
}

export function getAdminGradingStats() {
  return request({
    url: 'rag/admin-grading/stats',
    method: 'get'
  })
}

export function clearAdminGradingKnowledge() {
  return request({
    url: 'rag/admin-grading/clear',
    method: 'delete'
  })
}

export function testAdminGradingSearch(question, topK = 3) {
  return request({
    url: 'rag/admin-grading/test',
    method: 'post',
    params: { question, topK }
  })
}

export function getAdminGradingMaterials() {
  return request({
    url: 'rag/admin-grading/materials',
    method: 'get'
  })
}

export function getAdminGradingTasks() {
  return request({
    url: 'rag/admin-grading/tasks',
    method: 'get'
  })
}
