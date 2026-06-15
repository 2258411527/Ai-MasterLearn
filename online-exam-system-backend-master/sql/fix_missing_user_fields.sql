-- 修复 t_user 表缺失字段的 SQL 脚本
-- 执行此脚本前请先备份数据库

USE db_exam;

-- 检查并添加 max_grade_count 字段
SET @dbname = DATABASE();
SET @tablename = 't_user';
SET @columnname = 'max_grade_count';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' INT DEFAULT 0 COMMENT ''最大可加入班级数量：0-无限制，1-一个班级，99-多个班级''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 can_add_grade 字段
SET @columnname = 'can_add_grade';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' TINYINT DEFAULT 0 COMMENT ''是否可以添加班级：0-否，1-是''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 can_delete 字段
SET @columnname = 'can_delete';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' TINYINT DEFAULT 1 COMMENT ''是否可删除：0-否，1-是''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 admin_level 字段（如果缺失）
SET @columnname = 'admin_level';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' INT DEFAULT NULL COMMENT ''管理员等级：0-非管理员，1-系统管理员，2-一级管理员，3-二级管理员''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加 audit_admin_level 字段（如果缺失）
SET @columnname = 'audit_admin_level';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' INT DEFAULT NULL COMMENT ''审核时选择的管理员等级：2-一级管理员，3-二级管理员''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 更新现有管理员用户数据
UPDATE t_user SET admin_level = 1, max_grade_count = 99, can_add_grade = 1, can_delete = 0 
WHERE role_id = 3 AND id = 1 AND admin_level IS NULL;

-- 显示更新后的表结构
DESC t_user;

-- 显示用户信息
SELECT u.id, u.user_name, u.real_name, u.role_id, u.admin_level, 
       u.max_grade_count, u.can_add_grade, u.can_delete, u.status
FROM t_user u
WHERE u.is_deleted = 0
ORDER BY u.role_id, u.id;

