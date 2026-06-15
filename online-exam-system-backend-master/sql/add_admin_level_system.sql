-- 多级管理员权限系统数据库修改脚本
-- 执行此SQL脚本前请先备份数据库

USE db_exam;

-- 1. 修改角色表，添加管理员等级
ALTER TABLE t_role ADD COLUMN admin_level INT DEFAULT NULL COMMENT '管理员等级：0-非管理员，1-系统管理员，2-一级管理员，3-二级管理员';

-- 2. 修改用户表，添加管理员等级字段
ALTER TABLE t_user ADD COLUMN admin_level INT DEFAULT NULL COMMENT '管理员等级：0-非管理员，1-系统管理员，2-一级管理员，3-二级管理员';

-- 3. 修改用户表，添加班级数量限制字段
ALTER TABLE t_user ADD COLUMN max_grade_count INT DEFAULT 0 COMMENT '最大可加入班级数量：0-无限制，1-一个班级，99-多个班级';

-- 4. 修改用户表，添加是否可添加班级字段
ALTER TABLE t_user ADD COLUMN can_add_grade TINYINT DEFAULT 0 COMMENT '是否可以添加班级：0-否，1-是';

-- 5. 修改用户表，添加是否可删除字段
ALTER TABLE t_user ADD COLUMN can_delete TINYINT DEFAULT 1 COMMENT '是否可删除：0-否，1-是';

-- 6. 修改用户表，添加审核时选择的管理员等级
ALTER TABLE t_user ADD COLUMN audit_admin_level INT DEFAULT NULL COMMENT '审核时选择的管理员等级：2-一级管理员，3-二级管理员';

-- 7. 更新角色表数据
UPDATE t_role SET admin_level = 0 WHERE id = 1; -- 学生
UPDATE t_role SET admin_level = 0 WHERE id = 2; -- 教师
UPDATE t_role SET admin_level = 1 WHERE id = 3; -- 管理员（默认为系统管理员）

-- 8. 更新现有管理员用户数据
-- 将现有管理员设置为系统管理员
UPDATE t_user SET admin_level = 1, max_grade_count = 99, can_add_grade = 1, can_delete = 0 
WHERE role_id = 3 AND id = 1;

-- 9. 创建管理员审核记录表
CREATE TABLE IF NOT EXISTS t_admin_audit (
  id INT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  user_id INT NOT NULL COMMENT '申请用户ID',
  auditor_id INT DEFAULT NULL COMMENT '审核人ID',
  admin_level INT NOT NULL COMMENT '申请的管理员等级：2-一级管理员，3-二级管理员',
  audit_status INT DEFAULT 0 COMMENT '审核状态：0-待审核，1-审核通过，2-审核拒绝',
  audit_remark VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  is_deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (id),
  KEY idx_user_id (user_id),
  KEY idx_auditor_id (auditor_id),
  KEY idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员审核记录表';

-- 10. 显示更新后的表结构
DESC t_role;
DESC t_user;
DESC t_admin_audit;

-- 11. 显示角色和管理员信息
SELECT r.id, r.role_name, r.code, r.admin_level, 
       COUNT(u.id) as user_count
FROM t_role r
LEFT JOIN t_user u ON r.id = u.role_id AND u.is_deleted = 0
GROUP BY r.id, r.role_name, r.code, r.admin_level
ORDER BY r.id;

-- 12. 显示管理员用户信息
SELECT u.id, u.user_name, u.real_name, u.role_id, u.admin_level, 
       u.max_grade_count, u.can_add_grade, u.can_delete, u.status
FROM t_user u
WHERE u.role_id = 3 AND u.is_deleted = 0
ORDER BY u.admin_level, u.id;