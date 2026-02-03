CREATE TABLE IF NOT EXISTS `t_audit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `role_code` varchar(50) DEFAULT NULL COMMENT '角色编码',
  `action` varchar(100) DEFAULT NULL COMMENT '操作名称',
  `resource` varchar(50) DEFAULT NULL COMMENT '资源名称',
  `resource_id` varchar(50) DEFAULT NULL COMMENT '资源ID',
  `detail` varchar(255) DEFAULT NULL COMMENT '详情',
  `result` int(11) DEFAULT '1' COMMENT '结果 1成功 2失败',
  `cost_ms` int(11) DEFAULT NULL COMMENT '耗时(ms)',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(255) DEFAULT NULL COMMENT 'UserAgent',
  `trace_id` varchar(50) DEFAULT NULL COMMENT '追踪ID',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_action` (`user_id`,`action`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统审计日志';
