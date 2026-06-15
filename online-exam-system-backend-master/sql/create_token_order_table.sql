CREATE TABLE IF NOT EXISTS `t_token_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(64) NOT NULL COMMENT '商户订单号',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `package_id` int(11) NOT NULL COMMENT '套餐ID',
  `package_name` varchar(100) DEFAULT NULL COMMENT '套餐名称',
  `tokens` int(11) NOT NULL COMMENT '购买Token数量',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额(元)',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待支付 1-已支付 2-已关闭 3-已退款',
  `trade_no` varchar(64) DEFAULT NULL COMMENT '支付宝交易号',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token支付订单表';
