-- ============================================
-- AI定制学习系统 - 数据库表结构
-- ============================================

-- 1. 学习计划主表
CREATE TABLE IF NOT EXISTS `t_study_plan` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` INT(11) NOT NULL COMMENT '用户ID',
  `plan_name` VARCHAR(200) NOT NULL COMMENT '计划名称',
  `target_date` DATE NOT NULL COMMENT '目标日期（考试日期）',
  `subjects` TEXT COMMENT '学习科目JSON数组',
  `daily_hours` DECIMAL(3,1) DEFAULT 2.0 COMMENT '每日学习小时数',
  `plan_content` TEXT COMMENT 'AI生成的完整计划内容',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-停用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习计划主表';

-- 2. 每日任务表
CREATE TABLE IF NOT EXISTS `t_daily_task` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` INT(11) NOT NULL COMMENT '用户ID',
  `task_date` DATE NOT NULL COMMENT '任务日期',
  `subject` VARCHAR(100) NOT NULL COMMENT '学习科目',
  `task_content` TEXT NOT NULL COMMENT '任务内容',
  `estimated_minutes` INT(11) DEFAULT 60 COMMENT '预计用时（分钟）',
  `priority` TINYINT(1) DEFAULT 2 COMMENT '优先级：1-高 2-中 3-低',
  `is_completed` TINYINT(1) DEFAULT 0 COMMENT '是否完成：0-未完成 1-已完成',
  `completed_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `ai_suggestion` TEXT COMMENT 'AI建议',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date_subject` (`user_id`, `task_date`, `subject`(50)),
  KEY `idx_user_date` (`user_id`, `task_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日学习任务表';

-- 3. 学习时长记录表
CREATE TABLE IF NOT EXISTS `t_study_duration` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` INT(11) NOT NULL COMMENT '用户ID',
  `study_date` DATE NOT NULL COMMENT '学习日期',
  `duration_minutes` INT(11) NOT NULL DEFAULT 0 COMMENT '学习时长（分钟）',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `subject` VARCHAR(100) DEFAULT NULL COMMENT '学习科目',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `study_date`),
  KEY `idx_user_date` (`user_id`, `study_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习时长记录表';

-- 4. 倒计时配置表
CREATE TABLE IF NOT EXISTS `t_countdown_config` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` INT(11) NOT NULL COMMENT '用户ID',
  `event_name` VARCHAR(200) NOT NULL COMMENT '事件名称（如：考研初试）',
  `target_date` DATE NOT NULL COMMENT '目标日期',
  `milestones` TEXT COMMENT '关键时间节点JSON数组',
  `ai_analysis` TEXT COMMENT 'AI分析建议',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否激活',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='倒计时配置表';

-- 5. 每日一题表
CREATE TABLE IF NOT EXISTS `t_daily_question` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` INT(11) NOT NULL COMMENT '用户ID',
  `question_date` DATE NOT NULL COMMENT '题目日期',
  `subject` VARCHAR(100) NOT NULL COMMENT '科目',
  `question_content` TEXT NOT NULL COMMENT '题目内容',
  `question_type` VARCHAR(20) DEFAULT 'choice' COMMENT '题目类型：choice-选择题 blank-填空题',
  `options` TEXT COMMENT '选项JSON数组（选择题用）',
  `correct_answer` TEXT NOT NULL COMMENT '正确答案',
  `analysis` TEXT COMMENT '答案解析',
  `user_answer` TEXT DEFAULT NULL COMMENT '用户答案',
  `is_correct` TINYINT(1) DEFAULT NULL COMMENT '是否正确：0-错误 1-正确 NULL-未答',
  `answered_time` DATETIME DEFAULT NULL COMMENT '答题时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date_subject` (`user_id`, `question_date`, `subject`),
  KEY `idx_user_date` (`user_id`, `question_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日一题表';

-- 6. 情绪分析表
CREATE TABLE IF NOT EXISTS `t_emotion_analysis` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` INT(11) NOT NULL COMMENT '用户ID',
  `analysis_date` DATE NOT NULL COMMENT '分析日期',
  `emotion_type` VARCHAR(50) NOT NULL COMMENT '情绪类型：happy-开心 calm-平静 anxious-焦虑 tired-疲惫 motivated-积极',
  `emotion_score` INT(11) DEFAULT 50 COMMENT '情绪分数（0-100）',
  `emoji` VARCHAR(10) DEFAULT '😊' COMMENT '表情符号',
  `chat_summary` TEXT COMMENT '聊天内容摘要',
  `ai_suggestion` TEXT COMMENT 'AI建议',
  `data_source` VARCHAR(50) DEFAULT 'all' COMMENT '数据来源：ai_chat-ai聊天 friend_chat-好友聊天 all-全部',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `analysis_date`),
  KEY `idx_user_date` (`user_id`, `analysis_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='情绪分析表';