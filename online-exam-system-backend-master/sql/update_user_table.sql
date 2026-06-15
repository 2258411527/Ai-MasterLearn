-- 更新用户表，添加审核相关字段
-- 执行此SQL脚本前请先备份数据库

USE db_exam;

-- 添加审核状态字段：0-待审核，1-审核通过，2-审核拒绝
ALTER TABLE t_user ADD COLUMN audit_status INT DEFAULT 1 COMMENT '审核状态：0-待审核，1-审核通过，2-审核拒绝';

-- 添加审核备注字段
ALTER TABLE t_user ADD COLUMN audit_remark VARCHAR(500) COMMENT '审核备注';

-- 添加审核时间字段
ALTER TABLE t_user ADD COLUMN audit_time DATETIME COMMENT '审核时间';

-- 添加审核人ID字段
ALTER TABLE t_user ADD COLUMN auditor_id INT COMMENT '审核人ID';

-- 更新现有用户的审核状态
-- 将现有用户的审核状态设置为1（审核通过）
UPDATE t_user SET audit_status = 1 WHERE audit_status IS NULL;

-- 为管理员和教师用户设置审核状态为1（已审核通过）
UPDATE t_user SET audit_status = 1 WHERE role_id IN (2, 3);

-- 为现有管理员用户设置审核信息（可选）
UPDATE t_user SET audit_time = create_time, auditor_id = 1 WHERE role_id = 3 AND audit_time IS NULL;

-- 显示更新后的表结构
DESC t_user;

-- 显示用户审核状态
SELECT id, user_name, real_name, role_id, audit_status, audit_remark, audit_time, auditor_id 
FROM t_user 
ORDER BY role_id, id;