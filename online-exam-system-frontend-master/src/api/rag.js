import request from '@/utils/request'

/**
 * 异步解析学习资料到知识库
 */
export function parseMaterialToKnowledge(materialId) {
  return request({
    url: 'rag/parse/' + materialId,
    method: 'post',
    timeout: 30000 // 30秒超时，因为是异步任务，只需等待任务提交
  })
}

/**
 * 解析学习资料到知识库（别名，兼容旧代码）
 */
export function parseMaterial(materialId) {
  return parseMaterialToKnowledge(materialId)
}

/**
 * 查询解析任务进度
 */
export function getTaskProgress(taskId) {
  return request({
    url: 'rag/task/' + taskId,
    method: 'get'
  })
}

/**
 * 获取用户的解析任务列表
 */
export function getUserTasks() {
  return request({
    url: 'rag/tasks',
    method: 'get'
  })
}

/**
 * 获取特定资料的最新解析任务
 */
export function getLatestTaskByMaterial(materialId) {
  return request({
    url: 'rag/task/material/' + materialId,
    method: 'get'
  })
}

/**
 * 获取知识库统计信息
 */
export function getKnowledgeStats() {
  return request({
    url: 'rag/stats',
    method: 'get'
  })
}

/**
 * 清空用户知识库
 */
export function clearKnowledge() {
  return request({
    url: 'rag/clear',
    method: 'delete'
  })
}

/**
 * 获取已解析的资料列表
 */
export function getParsedMaterials() {
  return request({
    url: 'rag/materials',
    method: 'get'
  })
}

/**
 * 删除特定资料的知识库数据
 */
export function deleteMaterialKnowledge(materialId) {
  return request({
    url: 'rag/material/' + materialId,
    method: 'delete'
  })
}

/**
 * 测试RAG检索
 */
export function testRagSearch(question, topK = 3) {
  return request({
    url: 'rag/test',
    method: 'post',
    params: {
      question,
      topK
    }
  })
}

/**
 * 获取资料的解析内容块
 */
export function getMaterialChunks(materialId) {
  return request({
    url: `rag/material/${materialId}/chunks`,
    method: 'get'
  })
}

/**
 * 删除单个解析任务
 */
export function deleteTask(taskId) {
  return request({
    url: `rag/task/${taskId}`,
    method: 'delete'
  })
}

/**
 * 批量删除解析任务
 */
export function deleteTasks(taskIds) {
  return request({
    url: 'rag/tasks/batch-delete',
    method: 'delete',
    data: taskIds
  })
}

/**
 * 获取所有学习资料（包括解析状态）
 */
export function getAllMaterials() {
  return request({
    url: 'rag/all-materials',
    method: 'get'
  })
}
