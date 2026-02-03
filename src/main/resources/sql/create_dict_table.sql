-- =============================================
-- 创建数据字典表
-- =============================================

CREATE TABLE IF NOT EXISTS `t_dict` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type_code` VARCHAR(50) NOT NULL COMMENT '字典类型编码',
  `type_name` VARCHAR(100) NOT NULL COMMENT '字典类型名称',
  `dict_code` VARCHAR(50) NOT NULL COMMENT '字典项编码',
  `dict_label` VARCHAR(100) NOT NULL COMMENT '字典项标签',
  `dict_value` VARCHAR(100) NOT NULL COMMENT '字典项值',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态 1正常 0停用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_type_code` (`type_code`),
  KEY `idx_dict_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- 插入一些示例数据
INSERT INTO `t_dict` (`type_code`, `type_name`, `dict_code`, `dict_label`, `dict_value`, `sort`, `status`, `created_by`, `updated_by`) VALUES
('user_status', '用户状态', 'normal', '正常', '1', 1, 1, 'system', 'system'),
('user_status', '用户状态', 'frozen', '冻结', '2', 2, 1, 'system', 'system'),
('user_type', '用户类型', 'admin', '管理员', '1', 1, 1, 'system', 'system'),
('user_type', '用户类型', 'doctor', '医生', '2', 2, 1, 'system', 'system'),
('user_type', '用户类型', 'student', '学生', '3', 3, 1, 'system', 'system'),
('gender', '性别', 'male', '男', '1', 1, 1, 'system', 'system'),
('gender', '性别', 'female', '女', '2', 2, 1, 'system', 'system'),
('exam_status', '体检状态', 'pending', '待体检', '0', 1, 1, 'system', 'system'),
('exam_status', '体检状态', 'completed', '已完成', '1', 2, 1, 'system', 'system'),
('abnormal_flag', '异常标识', 'normal', '正常', '0', 1, 1, 'system', 'system'),
('abnormal_flag', '异常标识', 'abnormal', '异常', '1', 2, 1, 'system', 'system');

-- 验证创建结果
SELECT '=== 数据字典表创建成功 ===' as info;
SELECT COUNT(*) as total_records FROM t_dict;
SELECT type_code, type_name, COUNT(*) as item_count 
FROM t_dict 
GROUP BY type_code, type_name;
