-- 为 t_user_exams_score 表添加 ai_graded 字段
-- 用于标记是否已进行AI阅卷 (0=未AI阅卷, 1=已AI阅卷)

ALTER TABLE `t_user_exams_score` 
ADD COLUMN `ai_graded` INT DEFAULT 0 COMMENT '是否AI阅卷：0=未AI阅卷，1=已AI阅卷' AFTER `whether_mark`;

-- 更新现有记录，将 ai_graded 设置为默认值 0
UPDATE `t_user_exams_score` SET `ai_graded` = 0 WHERE `ai_graded` IS NULL;
