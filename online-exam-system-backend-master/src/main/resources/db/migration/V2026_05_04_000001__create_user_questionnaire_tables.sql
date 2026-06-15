-- 用户注册问卷系统表结构

-- 1. 用户备考目标表
CREATE TABLE IF NOT EXISTS `t_user_study_goal` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `exam_type` varchar(20) NOT NULL COMMENT '考试类型：考研/考公/双线备考',
  `target_year` int(4) NOT NULL COMMENT '目标年份：2026/2027等',
  `study_identity` varchar(20) NOT NULL COMMENT '备考身份：应届本科/往届脱产/在职上班族',
  `daily_study_hours` varchar(20) NOT NULL COMMENT '每日学习时长：2h内/3-4h/5-7h/8h以上',
  `current_progress` varchar(20) NOT NULL COMMENT '当前备考进度：未开始/刚开始/备考中已有基础',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_exam_type` (`exam_type`),
  KEY `idx_target_year` (`target_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户备考目标表';

-- 2. 考研专属信息表
CREATE TABLE IF NOT EXISTS `t_user_kaoyan_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `attempt_count` varchar(20) NOT NULL COMMENT '考研届数：一战/二战/三战及以上',
  `degree_type` varchar(20) NOT NULL COMMENT '报考类型：学硕/专硕',
  `major_type` varchar(20) NOT NULL COMMENT '报考方式：本专业报考/跨专业报考',
  `target_university_level` varchar(20) NOT NULL COMMENT '目标院校层次：双非/省属重点/211/985',
  `english_subject` varchar(20) DEFAULT NULL COMMENT '英语科目：英语一/英语二',
  `math_subject` varchar(20) DEFAULT NULL COMMENT '数学科目：数学一/数学二/数学三/不考数学',
  `major_course_name` varchar(100) DEFAULT NULL COMMENT '专业课名称',
  `english_level` varchar(20) DEFAULT NULL COMMENT '英语基础：零基础/一般/较好',
  `math_level` varchar(20) DEFAULT NULL COMMENT '数学基础：零基础/一般/较好',
  `major_level` varchar(20) DEFAULT NULL COMMENT '专业课基础：零基础/遗忘较多/基础扎实',
  `weak_modules` text COMMENT '薄弱模块（JSON格式存储）',
  `study_needs` text COMMENT '备考需求倾向（JSON格式存储）',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_degree_type` (`degree_type`),
  KEY `idx_university_level` (`target_university_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考研专属信息表';

-- 3. 考公专属信息表
CREATE TABLE IF NOT EXISTS `t_user_kaogong_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `exam_category` varchar(20) NOT NULL COMMENT '考试类型：国考/省考/选调生/事业单位',
  `target_region` varchar(50) NOT NULL COMMENT '意向报考地区：本省/全国不限/特定省份',
  `position_category` varchar(20) NOT NULL COMMENT '意向岗位类别：综合文职/行政执法/基层乡镇/不限',
  `preparation_experience` varchar(20) NOT NULL COMMENT '备考经历：首次备考/曾参加过笔试/进过面试',
  `xingce_level` varchar(20) DEFAULT NULL COMMENT '行测基础：零基础/初学刷题/有实战基础',
  `shenlun_level` varchar(20) DEFAULT NULL COMMENT '申论基础：从未动笔/偶尔练习/经常写大作文',
  `weak_modules` text COMMENT '薄弱模块（JSON格式存储）',
  `study_needs` text COMMENT '备考需求倾向（JSON格式存储）',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_exam_category` (`exam_category`),
  KEY `idx_target_region` (`target_region`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考公专属信息表';

-- 4. 用户学习分析表
CREATE TABLE IF NOT EXISTS `t_user_study_analysis` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `total_study_days` int(11) DEFAULT 0 COMMENT '总学习天数',
  `total_study_hours` decimal(10,2) DEFAULT 0.00 COMMENT '总学习时长（小时）',
  `avg_daily_study_hours` decimal(5,2) DEFAULT 0.00 COMMENT '日均学习时长',
  `total_exams_taken` int(11) DEFAULT 0 COMMENT '参加考试总数',
  `total_questions_answered` int(11) DEFAULT 0 COMMENT '答题总数',
  `correct_rate` decimal(5,2) DEFAULT 0.00 COMMENT '正确率',
  `weak_knowledge_points` text COMMENT '薄弱知识点（JSON格式）',
  `strong_knowledge_points` text COMMENT '优势知识点（JSON格式）',
  `learning_trend` text COMMENT '学习趋势分析（JSON格式）',
  `ai_recommendations` text COMMENT 'AI推荐内容（JSON格式）',
  `last_analysis_time` datetime DEFAULT NULL COMMENT '最后分析时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_correct_rate` (`correct_rate`),
  KEY `idx_last_analysis_time` (`last_analysis_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户学习分析表';

-- 5. AI推荐内容表
CREATE TABLE IF NOT EXISTS `t_ai_recommendation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `recommendation_type` varchar(50) NOT NULL COMMENT '推荐类型：daily_plan/weak_point/exercise/review',
  `recommendation_content` text NOT NULL COMMENT '推荐内容（JSON格式）',
  `priority_level` int(2) DEFAULT 1 COMMENT '优先级：1-5，5为最高',
  `is_completed` tinyint(1) DEFAULT 0 COMMENT '是否完成：0-未完成，1-已完成',
  `completion_time` datetime DEFAULT NULL COMMENT '完成时间',
  `valid_until` datetime DEFAULT NULL COMMENT '有效期至',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_recommendation_type` (`recommendation_type`),
  KEY `idx_priority_level` (`priority_level`),
  KEY `idx_is_completed` (`is_completed`),
  KEY `idx_valid_until` (`valid_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI推荐内容表';