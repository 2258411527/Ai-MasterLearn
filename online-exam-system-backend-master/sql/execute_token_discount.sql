ALTER TABLE `t_token_package` ADD COLUMN `discount` DECIMAL(3,2) DEFAULT 1.00 COMMENT '折扣比例' AFTER `price`;
ALTER TABLE `t_token_package` ADD COLUMN `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价' AFTER `discount`;
SELECT * FROM t_token_package;
