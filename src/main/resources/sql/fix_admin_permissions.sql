-- =============================================
-- 修复管理员权限问题
-- 确保管理员拥有所有必要的权限
-- =============================================

-- 1. 确保所有权限都存在
INSERT IGNORE INTO t_permission (code, name, description, created_at, updated_at)
VALUES 
    ('analytics:read', '查看数据分析', '查看系统数据分析', NOW(), NOW()),
    ('exam_record:read', '查看体检记录', '查看体检记录', NOW(), NOW()),
    ('exam_record:create', '创建体检记录', '创建体检记录', NOW(), NOW()),
    ('exam_record:update', '更新体检记录', '更新体检记录', NOW(), NOW()),
    ('exam_record:delete', '删除体检记录', '删除体检记录', NOW(), NOW()),
    ('report:read', '查看报告', '查看健康报告', NOW(), NOW()),
    ('report:generate', '生成报告', '生成健康报告', NOW(), NOW()),
    ('report:update', '更新报告', '更新健康报告', NOW(), NOW()),
    ('report:export_pdf', '导出PDF', '导出PDF报告', NOW(), NOW()),
    ('report:export_excel', '导出Excel', '导出Excel报告', NOW(), NOW()),
    ('student:read', '查看学生', '查看学生信息', NOW(), NOW()),
    ('student:create', '创建学生', '创建学生信息', NOW(), NOW()),
    ('student:update', '更新学生', '更新学生信息', NOW(), NOW()),
    ('student:delete', '删除学生', '删除学生信息', NOW(), NOW());

-- 2. 清除管理员角色的旧权限映射
DELETE FROM t_role_permission 
WHERE role_id = (SELECT id FROM t_role WHERE code = 'ROLE_ADMIN');

-- 3. 为管理员角色分配所有权限
INSERT INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM t_role r
CROSS JOIN t_permission p
WHERE r.code = 'ROLE_ADMIN';

-- 4. 验证管理员权限
SELECT 
    r.code as role_code,
    r.name as role_name,
    COUNT(p.id) as permission_count
FROM t_role r
LEFT JOIN t_role_permission rp ON r.id = rp.role_id
LEFT JOIN t_permission p ON rp.permission_id = p.id
WHERE r.code = 'ROLE_ADMIN'
GROUP BY r.id, r.code, r.name;

-- 5. 查看管理员用户的所有权限
SELECT 
    u.username,
    r.code as role_code,
    p.code as permission_code,
    p.name as permission_name
FROM t_user u
JOIN t_user_role ur ON u.id = ur.user_id
JOIN t_role r ON ur.role_id = r.id
JOIN t_role_permission rp ON r.id = rp.role_id
JOIN t_permission p ON rp.permission_id = p.id
WHERE u.username = 'root'
ORDER BY p.code;

-- =============================================
-- 执行完成后，请：
-- 1. 退出登录
-- 2. 清除浏览器缓存
-- 3. 重新登录
-- =============================================
