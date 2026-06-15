-- 为t_discussion表添加可见范围字段
-- 执行时间：2026-05-01
-- 说明：支持用户选择讨论的可见范围（公开或指定班级）

-- 1. 添加visibility字段
ALTER TABLE `t_discussion` 
ADD COLUMN `visibility` tinyint(1) NOT NULL DEFAULT 0 COMMENT '可见范围：0-指定班级可见，1-公开可见' AFTER `title`;

-- 2. 修改grade_id为可空（公开讨论不需要指定班级）
ALTER TABLE `t_discussion` 
MODIFY COLUMN `grade_id` int(11) DEFAULT NULL COMMENT '接收班级id（公开时可为NULL）';

-- 3. 更新现有数据（默认设置为指定班级可见）
UPDATE `t_discussion` SET `visibility` = 0 WHERE `visibility` IS NULL;
