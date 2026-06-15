-- 创建RAG知识库表
CREATE TABLE IF NOT EXISTS `rag_knowledge` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `study_material_id` int(11) NOT NULL COMMENT '学习资料ID',
  `chunk_content` text NOT NULL COMMENT '文本块内容',
  `chunk_index` int(11) NOT NULL COMMENT '文本块索引',
  `embedding_vector` text COMMENT '向量嵌入（JSON格式存储）',
  `metadata` text COMMENT '元数据（JSON格式）',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_study_material_id` (`study_material_id`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RAG知识库表';

-- 创建向量索引（如果需要支持向量搜索）
-- ALTER TABLE `rag_knowledge` ADD SPATIAL INDEX `idx_embedding` (`embedding_vector`);