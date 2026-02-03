CREATE TABLE IF NOT EXISTS `t_dict` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `type_code` varchar(50) NOT NULL COMMENT '字典类型',
  `label` varchar(100) NOT NULL COMMENT '显示名',
  `value` varchar(100) NOT NULL COMMENT '存储值',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `status` int(11) DEFAULT '0' COMMENT '状态 0正常 1禁用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `created_by` varchar(50) DEFAULT NULL COMMENT '创建者',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  `updated_by` varchar(50) DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典';

-- Init Data (Optional)
INSERT INTO `t_dict` (`type_code`, `label`, `value`, `sort`, `status`, `created_at`) VALUES 
('gender', '男', '1', 1, 0, NOW()),
('gender', '女', '2', 2, 0, NOW());
