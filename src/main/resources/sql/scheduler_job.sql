CREATE TABLE IF NOT EXISTS `t_scheduler_job` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(100) NOT NULL COMMENT '任务名称',
  `job_group` varchar(100) DEFAULT 'DEFAULT' COMMENT '任务组',
  `bean_name` varchar(100) NOT NULL COMMENT 'Spring Bean名称',
  `method_name` varchar(100) NOT NULL COMMENT '方法名称',
  `method_params` varchar(255) DEFAULT NULL COMMENT '方法参数',
  `cron_expression` varchar(100) NOT NULL COMMENT 'Cron表达式',
  `status` int(11) DEFAULT '0' COMMENT '状态 0正常 1暂停',
  `concurrent` int(11) DEFAULT '0' COMMENT '是否并发 0允许 1禁止',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  `updated_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务';
