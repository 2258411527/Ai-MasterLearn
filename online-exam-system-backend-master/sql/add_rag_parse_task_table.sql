-- ============================================
-- RAG解析任务表 - 用于跟踪异步解析进度
-- ============================================

CREATE TABLE IF NOT EXISTS `rag_parse_task` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` INT NOT NULL COMMENT '用户ID',
  `study_material_id` INT NOT NULL COMMENT '学习资料ID',
  `task_status` TINYINT NOT NULL DEFAULT 0 COMMENT '任务状态: 0-等待处理, 1-处理中, 2-已完成, 3-失败',
  `progress` INT NOT NULL DEFAULT 0 COMMENT '进度百分比(0-100)',
  `total_pages` INT DEFAULT NULL COMMENT '总页数(仅PDF)',
  `processed_pages` INT NOT NULL DEFAULT 0 COMMENT '已处理页数',
  `total_chunks` INT DEFAULT NULL COMMENT '总分块数',
  `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_study_material_id` (`study_material_id`),
  KEY `idx_task_status` (`task_status`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RAG解析任务表';

-- 添加索引以提高查询性能
ALTER TABLE `rag_parse_task` ADD INDEX `idx_user_material` (`user_id`, `study_material_id`);
