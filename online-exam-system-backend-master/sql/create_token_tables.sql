CREATE TABLE IF NOT EXISTS `t_user_token` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `balance` int(11) NOT NULL DEFAULT 0 COMMENT 'Token余额',
  `total_purchased` int(11) NOT NULL DEFAULT 0 COMMENT '累计购买Token数',
  `total_consumed` int(11) NOT NULL DEFAULT 0 COMMENT '累计消耗Token数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户Token余额表';

CREATE TABLE IF NOT EXISTS `t_token_transaction` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `type` tinyint(4) NOT NULL COMMENT '交易类型: 1-充值 2-消费 3-管理员调整',
  `amount` int(11) NOT NULL COMMENT '变动数量(正数)',
  `balance_after` int(11) NOT NULL COMMENT '变动后余额',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `ai_service_type` varchar(50) DEFAULT NULL COMMENT 'AI服务类型(chat/enhanced_chat/question_chat/analyze)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token交易记录表';

CREATE TABLE IF NOT EXISTS `t_token_package` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '套餐名称',
  `tokens` int(11) NOT NULL COMMENT 'Token数量',
  `price` decimal(10,2) NOT NULL COMMENT '价格(元)',
  `description` varchar(255) DEFAULT NULL COMMENT '套餐描述',
  `is_active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用: 0-否 1-是',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token套餐表';

INSERT INTO `t_token_package` (`name`, `tokens`, `price`, `description`, `is_active`, `sort_order`) VALUES
('体验包', 100, 9.90, '100 Tokens，适合轻度体验', 1, 1),
('基础包', 500, 39.90, '500 Tokens，日常学习够用', 1, 2),
('进阶包', 2000, 129.00, '2000 Tokens，深度学习首选', 1, 3),
('专业包', 5000, 269.00, '5000 Tokens，专业备考利器', 1, 4);
