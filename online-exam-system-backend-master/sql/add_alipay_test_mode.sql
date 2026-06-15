ALTER TABLE `t_alipay_config` ADD COLUMN `test_mode` tinyint(1) NOT NULL DEFAULT 0 COMMENT '测试模式: 0-否 1-是' AFTER `sign_type`;
