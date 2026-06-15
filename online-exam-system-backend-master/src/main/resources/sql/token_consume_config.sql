-- Token消耗配置表
CREATE TABLE IF NOT EXISTS `t_token_consume_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(50) NOT NULL COMMENT '配置键',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `token_cost` int(11) NOT NULL DEFAULT '1' COMMENT 'token消耗值',
  `description` varchar(255) DEFAULT NULL COMMENT '描述说明',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token消耗配置表';

-- 初始化默认配置数据
INSERT INTO `t_token_consume_config` (`config_key`, `config_name`, `token_cost`, `description`, `is_enabled`) VALUES
('ai_chat', 'AI对话(每次)', 1, '每次AI对话消耗的token数', 1),
('ai_chat_per_100_chars', 'AI对话(每100字)', 1, 'AI对话按输入字数计算，每100字消耗', 1),
('ai_grading', 'AI阅卷(每题)', 2, 'AI自动阅卷每题消耗的token数', 1),
('ai_analysis', '学习分析报告', 5, '生成AI学习分析报告消耗的token数', 1),
('ai_recommend', '个性化推荐', 3, '生成AI个性化推荐消耗的token数', 1),
('question_generate', 'AI生成题目', 5, 'AI生成练习题消耗的token数', 1),
('knowledge_qa', '知识点问答', 2, '知识点智能问答消耗的token数', 1);
