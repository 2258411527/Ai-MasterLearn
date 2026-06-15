ALTER TABLE `t_notice` ADD COLUMN `notice_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '消息类型: 1-系统公告 2-考试通知 3-成绩通知 4-活动通知' AFTER `content`;

CREATE TABLE IF NOT EXISTS `t_notice_read` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `notice_id` int(11) NOT NULL COMMENT '公告ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `read_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notice_user` (`notice_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告已读记录表';
