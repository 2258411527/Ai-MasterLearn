import request from '@/utils/request'

export function parseGradingMaterial(materialId) {
  return request({
    url: `/grading-rag/parse/${materialId}`,
    method: 'post'
  })
}

export function getGradingTaskProgress(taskId) {
  return request({
    url: `/grading-rag/task/${taskId}`,
    method: 'get'
  })
}

export function getGradingTasks() {
  return request({
    url: '/grading-rag/tasks',
    method: 'get'
  })
}

export function getGradingMaterials() {
  return request({
    url: '/grading-rag/materials',
    method: 'get'
  })
}

export function getGradingKnowledgeStats() {
  return request({
    url: '/grading-rag/stats',
    method: 'get'
  })
}

export function clearGradingKnowledge() {
  return request({
    url: '/grading-rag/clear',
    method: 'delete'
  })
}

export function deleteGradingMaterialKnowledge(materialId) {
  return request({
    url: `/grading-rag/material/${materialId}`,
    method: 'delete'
  })
}