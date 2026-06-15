-- 交流中心统计数据优化脚本
-- 执行时间：2026-05-04
-- 说明：在t_discussion和t_reply表中添加统计字段，提升查询性能

-- 1. 为t_discussion表添加统计字段
ALTER TABLE t_discussion 
ADD COLUMN like_count INT DEFAULT 0 COMMENT '点赞数',
ADD COLUMN comment_count INT DEFAULT 0 COMMENT '评论数',
ADD COLUMN view_count INT DEFAULT 0 COMMENT '浏览数';

-- 2. 为t_reply表添加统计字段
ALTER TABLE t_reply 
ADD COLUMN like_count INT DEFAULT 0 COMMENT '点赞数';

-- 3. 初始化现有数据的统计数据
-- 初始化话题的点赞数
UPDATE t_discussion d 
SET d.like_count = (
    SELECT COUNT(*) 
    FROM t_like l 
    WHERE l.discussion_id = d.id AND l.reply_id IS NULL
);

-- 初始化话题的评论数
UPDATE t_discussion d 
SET d.comment_count = (
    SELECT COUNT(*) 
    FROM t_reply r 
    WHERE r.discussion_id = d.id AND r.parent_id = -1
);

-- 初始化评论的点赞数
UPDATE t_reply r 
SET r.like_count = (
    SELECT COUNT(*) 
    FROM t_like l 
    WHERE l.reply_id = r.id
);

-- 4. 添加索引优化查询性能
CREATE INDEX idx_discussion_visibility ON t_discussion(visibility);
CREATE INDEX idx_reply_discussion_parent ON t_reply(discussion_id, parent_id);
CREATE INDEX idx_like_user_discussion ON t_like(user_id, discussion_id, reply_id);
