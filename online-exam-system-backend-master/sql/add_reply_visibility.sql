-- 为t_reply表添加可见范围字段
-- 执行时间：2026-05-01
-- 说明：支持用户选择评论的可见范围（公开或指定班级）

-- 1. 添加visibility字段
ALTER TABLE `t_reply` 
ADD COLUMN `visibility` tinyint(1) NOT NULL DEFAULT 0 COMMENT '可见范围：0-指定班级可见，1-公开可见' AFTER `content`;

-- 2. 更新现有数据（默认设置为公开可见）
UPDATE `t_reply` SET `visibility` = 1 WHERE `visibility` IS NULL;