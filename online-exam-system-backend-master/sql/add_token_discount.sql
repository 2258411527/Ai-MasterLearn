-- 为Token套餐表添加折扣字段
ALTER TABLE `t_token_package` ADD COLUMN `discount` DECIMAL(3,2) DEFAULT 1.00 COMMENT '折扣比例，如0.80表示8折，1.00表示无折扣' AFTER `price`;
ALTER TABLE `t_token_package` ADD COLUMN `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价（用于显示划线价）' AFTER `discount`;
