-- 添加 knowledge_point 字段到 t_question 表
-- 修复 Unknown column 'knowledge_point' 错误

-- 检查字段是否存在，如果不存在则添加
SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_question'
    AND COLUMN_NAME = 'knowledge_point'
);

-- 如果字段不存在，则添加
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE t_question ADD COLUMN knowledge_point VARCHAR(500) DEFAULT NULL COMMENT "知识点" AFTER repo_id',
    'SELECT "Column already exists"'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证字段添加成功
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 't_question'
AND COLUMN_NAME = 'knowledge_point';
