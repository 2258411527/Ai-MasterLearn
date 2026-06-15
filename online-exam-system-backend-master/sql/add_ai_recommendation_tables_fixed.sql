-- ============================================
-- AI个性化推荐系统 - 新增数据库表结构
-- ============================================

-- 1. AI每日推荐主表
CREATE TABLE IF NOT EXISTS `t_ai_daily_recommendation` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` INT(11) NOT NULL COMMENT '用户ID',
  `recommendation_date` DATE NOT NULL COMMENT '推荐日期',
  `recommendation_type` VARCHAR(50) NOT NULL COMMENT '推荐类型',
  `ai_content` JSON NOT NULL COMMENT 'AI生成的推荐内容',
  `ai_model` VARCHAR(100) DEFAULT 'qwen-turbo' COMMENT '使用的AI模型',
  `token_usage` INT(11) DEFAULT 0 COMMENT 'token使用量',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否有效',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date_type` (`user_id`, `recommendation_date`, `recommendation_type`),
  KEY `idx_user_date` (`user_id`, `recommendation_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI每日推荐主表';

-- 2. AI知识点推荐详情表
CREATE TABLE IF NOT EXISTS `t_ai_knowledge_points` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `recommendation_id` INT(11) NOT NULL COMMENT '推荐ID',
  `title` VARCHAR(200) NOT NULL COMMENT '知识点标题',
  `description` TEXT NOT NULL COMMENT '知识点描述',
  `subject` VARCHAR(100) NOT NULL COMMENT '所属科目',
  `priority_level` TINYINT(1) DEFAULT 2 COMMENT '优先级',
  `tags` JSON COMMENT '标签JSON数组',
  `ai_analysis` TEXT COMMENT 'AI分析说明',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_recommendation` (`recommendation_id`),
  KEY `idx_subject` (`subject`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI知识点推荐详情表';

-- 3. AI学习路径阶段表
CREATE TABLE IF NOT EXISTS `t_ai_learning_path` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `recommendation_id` INT(11) NOT NULL COMMENT '推荐ID',
  `stage_order` INT(11) NOT NULL COMMENT '阶段顺序',
  `stage_name` VARCHAR(100) NOT NULL COMMENT '阶段名称',
  `duration` VARCHAR(50) NOT NULL COMMENT '阶段时长',
  `goals` JSON NOT NULL COMMENT '阶段目标JSON数组',
  `ai_suggestion` TEXT COMMENT 'AI阶段建议',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_recommendation` (`recommendation_id`),
  KEY `idx_order` (`stage_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI学习路径阶段表';

-- 4. AI错题分析表
CREATE TABLE IF NOT EXISTS `t_ai_wrong_analysis` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `recommendation_id` INT(11) NOT NULL COMMENT '推荐ID',
  `weak_points` JSON NOT NULL COMMENT '薄弱知识点JSON数组',
  `improvement_areas` JSON NOT NULL COMMENT '需要改进的领域JSON数组',
  `ai_suggestion` TEXT NOT NULL COMMENT 'AI改进建议',
  `analysis_date` DATE NOT NULL COMMENT '分析日期',
  `wrong_count` INT(11) DEFAULT 0 COMMENT '错题数量',
  `accuracy_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '正确率',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_recommendation` (`recommendation_id`),
  KEY `idx_date` (`analysis_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI错题分析表';

-- 5. AI图书推荐表
CREATE TABLE IF NOT EXISTS `t_ai_book_recommendation` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `recommendation_id` INT(11) NOT NULL COMMENT '推荐ID',
  `book_title` VARCHAR(200) NOT NULL COMMENT '图书标题',
  `author` VARCHAR(100) NOT NULL COMMENT '作者',
  `recommendation_reason` TEXT NOT NULL COMMENT '推荐理由',
  `book_category` VARCHAR(100) COMMENT '图书分类',
  `difficulty_level` TINYINT(1) DEFAULT 2 COMMENT '难度等级',
  `mood_based` VARCHAR(50) COMMENT '基于的心情类型',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_recommendation` (`recommendation_id`),
  KEY `idx_category` (`book_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI图书推荐表';

-- 6. AI API调用记录表
CREATE TABLE IF NOT EXISTS `t_ai_api_log` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` INT(11) NOT NULL COMMENT '用户ID',
  `api_type` VARCHAR(50) NOT NULL COMMENT 'API类型',
  `request_content` TEXT COMMENT '请求内容',
  `response_content` TEXT COMMENT '响应内容',
  `ai_model` VARCHAR(100) DEFAULT 'qwen-turbo' COMMENT '使用的AI模型',
  `token_usage` INT(11) DEFAULT 0 COMMENT 'token使用量',
  `response_time` INT(11) DEFAULT 0 COMMENT '响应时间',
  `status` VARCHAR(20) DEFAULT 'success' COMMENT '状态',
  `error_message` TEXT COMMENT '错误信息',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `create_time`),
  KEY `idx_api_type` (`api_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI API调用记录表';