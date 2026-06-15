-- AI 配置表结构
DROP TABLE IF EXISTS `t_ai_config`;
CREATE TABLE `t_ai_config` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_name` varchar(100) NOT NULL COMMENT '配置名称',
    `api_key` varchar(500) NOT NULL COMMENT 'API密钥',
    `base_url` varchar(500) NOT NULL COMMENT '基础URL',
    `model` varchar(100) NOT NULL COMMENT '模型名称',
    `is_active` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否激活(0:未激活, 1:已激活)',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` int DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` int DEFAULT NULL COMMENT '更新人ID',
    `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除(0:未删除, 1:已删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_name` (`config_name`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI配置表';

-- 默认 AI 配置 - 阿里百炼 DashScope + Qwen3.5-122B
INSERT INTO `t_ai_config` (`config_name`, `api_key`, `base_url`, `model`, `is_active`, `create_time`, `update_time`)
VALUES (
    '阿里百炼-Qwen3.5-122B',
    'sk-5c430ad5547544f7bb656f20a0b48a27',
    'https://dashscope.aliyuncs.com/compatible-mode/v1',
    'qwen3.5-122b-a10b',
    1,
    NOW(),
    NOW()
);