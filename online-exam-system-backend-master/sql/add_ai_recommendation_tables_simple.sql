-- AI个性化推荐系统 - 数据库表结构

-- 1. AI每日推荐主表
CREATE TABLE IF NOT EXISTS `t_ai_daily_recommendation` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `user_id` INT(11) NOT NULL,
  `recommendation_date` DATE NOT NULL,
  `recommendation_type` VARCHAR(50) NOT NULL,
  `ai_content` JSON NOT NULL,
  `ai_model` VARCHAR(100) DEFAULT 'qwen-turbo',
  `token_usage` INT(11) DEFAULT 0,
  `is_active` TINYINT(1) DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date_type` (`user_id`, `recommendation_date`, `recommendation_type`),
  KEY `idx_user_date` (`user_id`, `recommendation_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. AI知识点推荐详情表
CREATE TABLE IF NOT EXISTS `t_ai_knowledge_points` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `recommendation_id` INT(11) NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `description` TEXT NOT NULL,
  `subject` VARCHAR(100) NOT NULL,
  `priority_level` TINYINT(1) DEFAULT 2,
  `tags` JSON,
  `ai_analysis` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_recommendation` (`recommendation_id`),
  KEY `idx_subject` (`subject`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. AI学习路径阶段表
CREATE TABLE IF NOT EXISTS `t_ai_learning_path` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `recommendation_id` INT(11) NOT NULL,
  `stage_order` INT(11) NOT NULL,
  `stage_name` VARCHAR(100) NOT NULL,
  `duration` VARCHAR(50) NOT NULL,
  `goals` JSON NOT NULL,
  `ai_suggestion` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_recommendation` (`recommendation_id`),
  KEY `idx_order` (`stage_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. AI错题分析表
CREATE TABLE IF NOT EXISTS `t_ai_wrong_analysis` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `recommendation_id` INT(11) NOT NULL,
  `weak_points` JSON NOT NULL,
  `improvement_areas` JSON NOT NULL,
  `ai_suggestion` TEXT NOT NULL,
  `analysis_date` DATE NOT NULL,
  `wrong_count` INT(11) DEFAULT 0,
  `accuracy_rate` DECIMAL(5,2) DEFAULT 0.00,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_recommendation` (`recommendation_id`),
  KEY `idx_date` (`analysis_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. AI图书推荐表
CREATE TABLE IF NOT EXISTS `t_ai_book_recommendation` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `recommendation_id` INT(11) NOT NULL,
  `book_title` VARCHAR(200) NOT NULL,
  `author` VARCHAR(100) NOT NULL,
  `recommendation_reason` TEXT NOT NULL,
  `book_category` VARCHAR(100),
  `difficulty_level` TINYINT(1) DEFAULT 2,
  `mood_based` VARCHAR(50),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_recommendation` (`recommendation_id`),
  KEY `idx_category` (`book_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. AI API调用记录表
CREATE TABLE IF NOT EXISTS `t_ai_api_log` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `user_id` INT(11) NOT NULL,
  `api_type` VARCHAR(50) NOT NULL,
  `request_content` TEXT,
  `response_content` TEXT,
  `ai_model` VARCHAR(100) DEFAULT 'qwen-turbo',
  `token_usage` INT(11) DEFAULT 0,
  `response_time` INT(11) DEFAULT 0,
  `status` VARCHAR(20) DEFAULT 'success',
  `error_message` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `create_time`),
  KEY `idx_api_type` (`api_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;