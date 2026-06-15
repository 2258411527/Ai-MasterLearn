-- User questionnaire related table structure
-- Create user study goal table
DROP TABLE IF EXISTS `t_user_study_goal`;
CREATE TABLE `t_user_study_goal` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key ID',
  `user_id` int(11) NOT NULL COMMENT 'User ID',
  `exam_type` varchar(50) DEFAULT NULL COMMENT 'Exam Type: Kaoyan/Civil Service/Both',
  `target_year` varchar(10) DEFAULT NULL COMMENT 'Target Year',
  `study_identity` varchar(50) DEFAULT NULL COMMENT 'Study Identity: Fresh Graduate/Previous Graduate/Working Professional',
  `daily_study_hours` varchar(20) DEFAULT NULL COMMENT 'Daily Study Hours: 2h内/3-4h/5-7h/8h以上',
  `current_progress` varchar(200) DEFAULT NULL COMMENT 'Current Progress Description',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT 'Logical Delete: 0-Not Deleted, 1-Deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_exam_type` (`exam_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='User Study Goal Table';

-- Create user kaoyan info table
DROP TABLE IF EXISTS `t_user_kaoyan_info`;
CREATE TABLE `t_user_kaoyan_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key ID',
  `user_id` int(11) NOT NULL COMMENT 'User ID',
  `attempt_count` varchar(20) DEFAULT NULL COMMENT 'Kaoyan Attempt Count: 1-First/2-Second/3-Third or More',
  `degree_type` varchar(50) DEFAULT NULL COMMENT 'Degree Type: Academic/Professional',
  `major_type` varchar(50) DEFAULT NULL COMMENT 'Major Type: Same Major/Cross Major',
  `target_university_level` varchar(50) DEFAULT NULL COMMENT 'Target University Level: 985/211/Double First-Class/Regular',
  `english_subject` varchar(50) DEFAULT NULL COMMENT 'English Subject: English I/English II',
  `math_subject` varchar(50) DEFAULT NULL COMMENT 'Math Subject: Math I/Math II/Math III/No Math',
  `major_course_name` varchar(100) DEFAULT NULL COMMENT 'Major Course Name',
  `english_level` varchar(50) DEFAULT NULL COMMENT 'English Level: CET-4/CET-6/IELTS/TOEFL',
  `math_level` varchar(50) DEFAULT NULL COMMENT 'Math Level: Weak/Average/Strong',
  `major_level` varchar(50) DEFAULT NULL COMMENT 'Major Level: Weak/Average/Strong',
  `weak_modules` text DEFAULT NULL COMMENT 'Weak Modules (JSON format)',
  `study_needs` text DEFAULT NULL COMMENT 'Study Needs (JSON format)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT 'Logical Delete: 0-Not Deleted, 1-Deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='User Kaoyan Info Table';

-- Create user kaogong info table
DROP TABLE IF EXISTS `t_user_kaogong_info`;
CREATE TABLE `t_user_kaogong_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key ID',
  `user_id` int(11) NOT NULL COMMENT 'User ID',
  `exam_category` varchar(50) DEFAULT NULL COMMENT 'Exam Category: National/Provincial/Public Institution',
  `target_region` varchar(100) DEFAULT NULL COMMENT 'Target Region',
  `position_category` varchar(50) DEFAULT NULL COMMENT 'Position Category: Comprehensive Management/Enforcement/Technical',
  `preparation_experience` varchar(50) DEFAULT NULL COMMENT 'Preparation Experience: First Time/Experienced',
  `xingce_level` varchar(50) DEFAULT NULL COMMENT 'Xingce Level: Weak/Average/Strong',
  `shenlun_level` varchar(50) DEFAULT NULL COMMENT 'Shenlun Level: Weak/Average/Strong',
  `weak_modules` text DEFAULT NULL COMMENT 'Weak Modules (JSON format)',
  `study_needs` text DEFAULT NULL COMMENT 'Study Needs (JSON format)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT 'Logical Delete: 0-Not Deleted, 1-Deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='User Kaogong Info Table';

-- Create user study analysis table
DROP TABLE IF EXISTS `t_user_study_analysis`;
CREATE TABLE `t_user_study_analysis` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key ID',
  `user_id` int(11) NOT NULL COMMENT 'User ID',
  `total_study_days` int(11) DEFAULT '0' COMMENT 'Total Study Days',
  `total_study_hours` double DEFAULT '0.0' COMMENT 'Total Study Hours',
  `avg_daily_study_hours` double DEFAULT '0.0' COMMENT 'Average Daily Study Hours',
  `total_exams_taken` int(11) DEFAULT '0' COMMENT 'Total Exams Taken',
  `total_questions_answered` int(11) DEFAULT '0' COMMENT 'Total Questions Answered',
  `correct_rate` double DEFAULT '0.0' COMMENT 'Correct Rate',
  `weak_knowledge_points` text DEFAULT NULL COMMENT 'Weak Knowledge Points (JSON format)',
  `strong_knowledge_points` text DEFAULT NULL COMMENT 'Strong Knowledge Points (JSON format)',
  `learning_trend` text DEFAULT NULL COMMENT 'Learning Trend (JSON format)',
  `ai_recommendations` text DEFAULT NULL COMMENT 'AI Recommendations (JSON format)',
  `last_analysis_time` datetime DEFAULT NULL COMMENT 'Last Analysis Time',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT 'Logical Delete: 0-Not Deleted, 1-Deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='User Study Analysis Table';

-- Create user profile table (for AI planning)
DROP TABLE IF EXISTS `t_user_profile`;
CREATE TABLE `t_user_profile` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Primary Key ID',
  `user_id` int(11) NOT NULL COMMENT 'User ID',
  `exam_type` varchar(50) DEFAULT NULL COMMENT 'Exam Type',
  `target_year` varchar(10) DEFAULT NULL COMMENT 'Target Year',
  `study_identity` varchar(50) DEFAULT NULL COMMENT 'Study Identity',
  `daily_study_hours` int(11) DEFAULT NULL COMMENT 'Daily Study Hours',
  `current_progress` varchar(200) DEFAULT NULL COMMENT 'Current Progress',
  `weak_modules` text DEFAULT NULL COMMENT 'Weak Modules (JSON format)',
  `needs` text DEFAULT NULL COMMENT 'Study Needs (JSON format)',
  `ai_plan` text DEFAULT NULL COMMENT 'AI Generated Study Plan (JSON format)',
  `plan_status` int(11) DEFAULT '0' COMMENT 'Plan Status: 0-Generating, 1-Generated, 2-Failed',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT 'Logical Delete: 0-Not Deleted, 1-Deleted',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='User Profile Table';