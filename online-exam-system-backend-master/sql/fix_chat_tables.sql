-- 修复好友关系和聊天消息表
-- 创建好友关系表
DROP TABLE IF EXISTS `friend_relation`;
CREATE TABLE `friend_relation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `friend_id` int(11) NOT NULL COMMENT '好友ID',
  `remark` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '好友备注',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '关系状态：0-待审核，1-已同意，2-已拒绝，3-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_friend_id` (`friend_id`) USING BTREE,
  KEY `idx_user_friend` (`user_id`,`friend_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='好友关系表';

-- 创建聊天消息表
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `from_user_id` int(11) NOT NULL COMMENT '发送者ID',
  `to_user_id` int(11) NOT NULL COMMENT '接收者ID',
  `content` text COLLATE utf8mb4_bin NOT NULL COMMENT '消息内容',
  `message_type` int(11) NOT NULL DEFAULT '1' COMMENT '消息类型：1-文本，2-图片，3-文件',
  `is_read` int(11) NOT NULL DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_from_user` (`from_user_id`) USING BTREE,
  KEY `idx_to_user` (`to_user_id`) USING BTREE,
  KEY `idx_user_pair` (`from_user_id`,`to_user_id`) USING BTREE,
  KEY `idx_is_read` (`is_read`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='聊天消息表';

-- 插入测试数据
INSERT INTO `friend_relation` (`user_id`, `friend_id`, `remark`, `status`, `create_time`, `update_time`) VALUES
(1, 2, '学生测试账号', 1, NOW(), NOW()),
(2, 1, '管理员', 1, NOW(), NOW()),
(1, 3, '教师测试账号', 1, NOW(), NOW()),
(3, 1, '管理员', 1, NOW(), NOW());

-- 插入聊天消息测试数据
INSERT INTO `chat_message` (`from_user_id`, `to_user_id`, `content`, `message_type`, `is_read`, `create_time`) VALUES
(1, 2, '你好，我是管理员！', 1, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 1, '管理员您好！很高兴认识您。', 1, 0, DATE_SUB(NOW(), INTERVAL 55 MINUTE)),
(1, 2, '最近学习怎么样？', 1, 0, DATE_SUB(NOW(), INTERVAL 50 MINUTE)),
(2, 1, '学习很顺利，谢谢关心！', 1, 0, DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(1, 3, '老师您好，有个问题想请教', 1, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(3, 1, '请说，我会尽力帮助您', 1, 0, DATE_SUB(NOW(), INTERVAL 25 MINUTE));

-- 创建索引优化查询性能
ALTER TABLE `friend_relation` ADD INDEX `idx_user_status` (`user_id`, `status`);
ALTER TABLE `friend_relation` ADD INDEX `idx_friend_status` (`friend_id`, `status`);
ALTER TABLE `chat_message` ADD INDEX `idx_conversation` (`from_user_id`, `to_user_id`, `create_time`);
ALTER TABLE `chat_message` ADD INDEX `idx_unread_messages` (`to_user_id`, `is_read`);