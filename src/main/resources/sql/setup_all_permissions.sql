-- =============================================
-- 完整权限配置 - 确保管理员拥有所有权限
-- =============================================

-- 1. 创建所有必需的权限
INSERT IGNORE INTO t_permission (code, name, description, created_at, updated_at) VALUES
-- 分析权限
('analytics:read', '查看数据分析', '查看系统数据分析', NOW(), NOW()),
-- 学生管理权限
('student:read', '查看学生', '查看学生信息', NOW(), NOW()),
('student:create', '创建学生', '创建学生信息', NOW(), NOW()),
('student:update', '更新学生', '更新学生信息', NOW(), NOW()),
('student:delete', '删除学生', '删除学生信息', NOW(), NOW()),
('student:import', '导入学生', '批量导入学生', NOW(), NOW()),
('student:export', '导出学生', '导出学生数据', NOW(), NOW()),
-- 体检记录权限
('exam_record:read', '查看体检记录', '查看体检记录', NOW(), NOW()),
('exam_record:create', '创建体检记录', '创建体检记录', NOW(), NOW()),
('exam_record:update', '更新体检记录', '更新体检记录', NOW(), NOW()),
('exam_record:delete', '删除体检记录', '删除体检记录', NOW(), NOW()),
-- 报告权限
('report:read', '查看报告', '查看健康报告', NOW(), NOW()),
('report:generate', '生成报告', '生成健康报告', NOW(), NOW()),
('report:update', '更新报告', '更新健康报告', NOW(), NOW()),
('report:export_pdf', '导出PDF', '导出PDF报告', NOW(), NOW()),
('report:export_excel', '导出Excel', '导出Excel报告', NOW(), NOW()),
-- 系统管理权限
('system:dict:create', '创建字典', '创建数据字典', NOW(), NOW()),
('system:dict:update', '更新字典', '更新数据字典', NOW(), NOW()),
('system:dict:delete', '删除字典', '删除数据字典', NOW(), NOW()),
('system:scheduler:read', '查看定时任务', '查看定时任务', NOW(), NOW()),
('system:scheduler:create', '创建定时任务', '创建定时任务', NOW(), NOW()),
('system:scheduler:update', '更新定时任务', '更新定时任务', NOW(), NOW()),
('system:scheduler:delete', '删除定时任务', '删除定时任务', NOW(), NOW()),
('system:scheduler:run', '执行定时任务', '手动执行定时任务', NOW(), NOW());

-- 2. 清除管理员角色的旧权限
DELETE FROM t_role_permission WHERE role_id = (SELECT id FROM t_role WHERE code = 'ROLE_ADMIN');

-- 3. 为管理员角色分配所有权限
INSERT INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM t_role r
CROSS JOIN t_permission p
WHERE r.code = 'ROLE_ADMIN';

-- 4. 验证权限配置
SELECT '=== 管理员角色拥有的权限数量 ===' as info;
SELECT 
    r.code as role_code,
    r.name as role_name,
    COUNT(p.id) as permission_count
FROM t_role r
LEFT JOIN t_role_permission rp ON r.id = rp.role_id
LEFT JOIN t_permission p ON rp.permission_id = p.id
WHERE r.code = 'ROLE_ADMIN'
GROUP BY r.id, r.code, r.name;

-- 5. 查看所有权限列表
SELECT '=== 所有权限列表 ===' as info;
SELECT code, name FROM t_permission ORDER BY code;

-- 6. 验证用户ID 9的权限
SELECT '=== 用户ID 9 的权限 ===' as info;
SELECT 
    u.id,
    u.username,
    r.code as role_code,
    COUNT(p.id) as permission_count
FROM t_user u
LEFT JOIN t_user_role ur ON u.id = ur.user_id
LEFT JOIN t_role r ON ur.role_id = r.id
LEFT JOIN t_role_permission rp ON r.id = rp.role_id
LEFT JOIN t_permission p ON rp.permission_id = p.id
WHERE u.id = 9
GROUP BY u.id, u.username, r.code;

-- =============================================
-- 执行完成后：
-- 1. 完全退出登录
-- 2. 清除浏览器缓存
-- 3. 重新登录
-- =============================================
