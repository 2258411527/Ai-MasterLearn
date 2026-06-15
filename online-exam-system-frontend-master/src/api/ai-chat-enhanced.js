import request from '@/utils/request'

/**
 * 增强AI对话（支持RAG检索）
 */
export function enhancedAiChat(data, enableRag = true, topK = 3) {
  return request({
    url: 'ai/chat/enhanced',
    method: 'post',
    data,
    params: {
      enableRag,
      topK
    }
  })
}
