-- 学生健康体检管理系统 - MySQL 8.0 初始化数据
-- 注意：password_hash 为占位，请在系统实现后通过“初始化管理员/重置密码”生成真实 BCrypt hash

USE student_health;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1) 角色
INSERT INTO t_role (code, name, description)
VALUES
('ROLE_ADMIN', '管理员', '全局配置、数据维护、审核、统计分析、任务运维'),
('ROLE_DOCTOR', '医生', '体检数据录入、报告生成与修订'),
('ROLE_STUDENT', '学生', '查看与下载个人报告')
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- 2) 权限点（与需求文档 02 对齐，按需增减）
INSERT INTO t_permission (code, name, description)
VALUES
('student:read', '学生-查询', NULL),
('student:create', '学生-新增', NULL),
('student:update', '学生-修改', NULL),
('student:delete', '学生-删除', NULL),
('student:import', '学生-导入', NULL),
('student:export', '学生-导出', NULL),

('exam_record:read', '体检记录-查询', NULL),
('exam_record:create', '体检记录-新增', NULL),
('exam_record:update', '体检记录-修改', NULL),
('exam_record:delete', '体检记录-删除', NULL),
('exam_record:import', '体检记录-导入', NULL),
('exam_record:export', '体检记录-导出', NULL),

('report:read', '报告-查询', NULL),
('report:generate', '报告-生成', NULL),
('report:update', '报告-修订', NULL),
('report:delete', '报告-删除', NULL),
('report:export_pdf', '报告-导出PDF', NULL),
('report:export_excel', '报告-导出Excel', NULL),
('report:print', '报告-打印', NULL),

('analytics:read', '统计分析-查看', NULL),
('analytics:export', '统计分析-导出', NULL),

('config:read', '系统配置-查看', NULL),
('config:update', '系统配置-修改', NULL),
('dict:manage', '字典阈值-管理', NULL),

('user:manage', '用户-管理', NULL),
('role:manage', '角色-管理', NULL),
('permission:manage', '权限-管理', NULL),

('audit:read', '审计-查看', NULL),
('audit:export', '审计-导出', NULL),
('audit:clear', '审计-清理', '慎用'),

('scheduler:read', '任务调度-查看', NULL),
('scheduler:manage', '任务调度-管理', NULL),
('scheduler:trigger', '任务调度-手动触发', NULL)
ON DUPLICATE KEY UPDATE name=VALUES(name), description=VALUES(description);

-- 3) 角色-权限绑定（默认策略）
-- 管理员：全部权限
INSERT IGNORE INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM t_role r
JOIN t_permission p
WHERE r.code='ROLE_ADMIN';

-- 医生：录入+报告+（可选）统计查看
INSERT IGNORE INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM t_role r
JOIN t_permission p
WHERE r.code='ROLE_DOCTOR'
  AND p.code IN (
    'student:read',
    'exam_record:read','exam_record:create','exam_record:update','exam_record:import','exam_record:export',
    'report:read','report:generate','report:update','report:export_pdf','report:export_excel','report:print',
    'analytics:read',
    'audit:read'
  );

-- 学生：只读本人（后端需做数据范围限制）
INSERT IGNORE INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM t_role r
JOIN t_permission p
WHERE r.code='ROLE_STUDENT'
  AND p.code IN (
    'student:read',
    'exam_record:read',
    'report:read','report:export_pdf','report:export_excel','report:print'
  );

-- 4) 默认管理员账号（占位）
-- 说明：password_hash 请替换为真实 BCrypt，例如 Spring Security BCryptPasswordEncoder.encode('admin123')
INSERT INTO t_user (username, display_name, user_type, password_hash, status)
VALUES
('admin', '系统管理员', 1, '$2a$10$REPLACE_WITH_REAL_BCRYPT_HASH___________________________', 1)
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), status=VALUES(status);

-- 绑定 admin -> ROLE_ADMIN
INSERT IGNORE INTO t_user_role (user_id, role_id)
SELECT u.id, r.id
FROM t_user u
JOIN t_role r ON r.code='ROLE_ADMIN'
WHERE u.username='admin';

-- 5) 指标字典（示例：核心体检项）
INSERT INTO t_metric_dict (metric_key, name, unit, value_type, required, enabled, sort_no, remark)
VALUES
('height', '身高', 'cm', 1, 1, 1, 10, NULL),
('weight', '体重', 'kg', 1, 1, 1, 20, NULL),
('bmi', 'BMI', NULL, 1, 0, 1, 30, '建议系统自动计算'),
('sbp', '收缩压', 'mmHg', 1, 1, 1, 40, NULL),
('dbp', '舒张压', 'mmHg', 1, 1, 1, 50, NULL),
('heart_rate', '心率', 'bpm', 1, 0, 1, 60, NULL),
('vision_l', '左眼视力', NULL, 1, 0, 1, 70, NULL),
('vision_r', '右眼视力', NULL, 1, 0, 1, 80, NULL)
ON DUPLICATE KEY UPDATE
  name=VALUES(name), unit=VALUES(unit), value_type=VALUES(value_type),
  required=VALUES(required), enabled=VALUES(enabled), sort_no=VALUES(sort_no), remark=VALUES(remark);

-- 6) 指标阈值（示例：可后续在后台配置更精细）
INSERT INTO t_metric_threshold (metric_key, gender, grade, age_min, age_max, ref_low, ref_high, remark)
VALUES
('height', NULL, NULL, NULL, NULL, 50, 250, '合理范围示例'),
('weight', NULL, NULL, NULL, NULL, 10, 300, '合理范围示例'),
('bmi', NULL, NULL, NULL, NULL, 10, 60, '合理范围示例'),
('sbp', NULL, NULL, NULL, NULL, 60, 250, '合理范围示例'),
('dbp', NULL, NULL, NULL, NULL, 30, 150, '合理范围示例'),
('heart_rate', NULL, NULL, NULL, NULL, 30, 220, '合理范围示例')
ON DUPLICATE KEY UPDATE ref_low=VALUES(ref_low), ref_high=VALUES(ref_high), remark=VALUES(remark);

-- 7) 内置任务（示例）
INSERT INTO t_scheduler_job (job_code, job_name, job_desc, cron_expr, status)
VALUES
('REPORT_GENERATE_DAILY', '定期报告生成', '每日补生成缺失报告', '0 0 2 * * ?', 1),
('AUDIT_LOG_CLEAN_MONTHLY', '系统日志清理', '每月清理历史审计日志（按策略）', '0 0 3 1 * ?', 2),
('CACHE_WARMUP_DAILY', '缓存预热', '每日预加载字典/阈值', '0 30 1 * * ?', 2)
ON DUPLICATE KEY UPDATE
  job_name=VALUES(job_name), job_desc=VALUES(job_desc),
  cron_expr=VALUES(cron_expr), status=VALUES(status);

SET FOREIGN_KEY_CHECKS = 1;



