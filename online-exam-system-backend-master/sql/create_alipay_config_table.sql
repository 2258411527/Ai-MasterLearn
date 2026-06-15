CREATE TABLE IF NOT EXISTS `t_alipay_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `app_id` varchar(64) NOT NULL COMMENT '应用AppId',
  `private_key` text NOT NULL COMMENT '应用私钥',
  `alipay_public_key` text NOT NULL COMMENT '支付宝公钥',
  `server_url` varchar(255) NOT NULL DEFAULT 'https://openapi.alipay.com/gateway.do' COMMENT '支付宝网关地址',
  `notify_url` varchar(255) DEFAULT NULL COMMENT '异步通知地址',
  `return_url` varchar(255) DEFAULT NULL COMMENT '同步跳转地址',
  `sign_type` varchar(20) NOT NULL DEFAULT 'RSA2' COMMENT '签名类型',
  `is_active` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否激活: 0-否 1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付宝配置表';
