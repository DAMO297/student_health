-- =============================================
-- 完整的管理员账号创建脚本
-- 包含角色和权限的初始化
-- =============================================

-- 第一步：创建角色（如果不存在）
-- =============================================
INSERT IGNORE INTO t_role (code, name, description, created_at, updated_at)
VALUES 
    ('ROLE_ADMIN', '管理员', '系统管理员角色', NOW(), NOW()),
    ('ROLE_DOCTOR', '医生', '医生角色', NOW(), NOW()),
    ('ROLE_STUDENT', '学生', '学生角色', NOW(), NOW());

-- 第二步：创建权限（如果不存在）
-- =============================================
INSERT IGNORE INTO t_permission (code, name, description, created_at, updated_at)
VALUES 
    -- 分析权限
    ('analytics:read', '查看数据分析', '查看系统数据分析', NOW(), NOW()),
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
    ('report:export_excel', '导出Excel', '导出Excel报告', NOW(), NOW());

-- 第三步：为管理员角色分配所有权限
-- =============================================
INSERT IGNORE INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM t_role r, t_permission p
WHERE r.code = 'ROLE_ADMIN';

-- 为医生角色分配权限
INSERT IGNORE INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM t_role r, t_permission p
WHERE r.code = 'ROLE_DOCTOR' 
AND p.code IN ('analytics:read', 'exam_record:read', 'exam_record:create', 'exam_record:update', 
               'report:read', 'report:generate', 'report:update', 'report:export_pdf');

-- 为学生角色分配权限
INSERT IGNORE INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM t_role r, t_permission p
WHERE r.code = 'ROLE_STUDENT' 
AND p.code IN ('report:read', 'report:export_pdf');

-- 第四步：清理可能存在的root用户
-- =============================================
DELETE FROM t_user_role WHERE user_id IN (SELECT id FROM t_user WHERE username = 'root');
DELETE FROM t_user WHERE username = 'root';

-- 第五步：创建管理员用户
-- =============================================
INSERT INTO t_user (
    username, 
    display_name, 
    user_type, 
    password_hash, 
    status, 
    token_version, 
    deleted,
    created_by, 
    updated_by, 
    created_at, 
    updated_at
) VALUES (
    'root',
    '系统管理员',
    1,
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    1,
    0,
    0,
    'system',
    'system',
    NOW(),
    NOW()
);

-- 第六步：为管理员分配角色
-- =============================================
INSERT INTO t_user_role (user_id, role_id)
SELECT 
    u.id,
    r.id
FROM t_user u, t_role r
WHERE u.username = 'root' 
AND r.code = 'ROLE_ADMIN';

-- 第七步：验证结果
-- =============================================
SELECT '=== 角色列表 ===' as info;
SELECT id, code, name FROM t_role;

SELECT '=== 管理员用户信息 ===' as info;
SELECT 
    u.id,
    u.username,
    u.display_name,
    u.user_type,
    u.status,
    r.code as role_code,
    r.name as role_name
FROM t_user u
LEFT JOIN t_user_role ur ON u.id = ur.user_id
LEFT JOIN t_role r ON ur.role_id = r.id
WHERE u.username = 'root';

SELECT '=== 管理员权限列表 ===' as info;
SELECT p.code, p.name
FROM t_user u
JOIN t_user_role ur ON u.id = ur.user_id
JOIN t_role_permission rp ON ur.role_id = rp.role_id
JOIN t_permission p ON rp.permission_id = p.id
WHERE u.username = 'root';

-- =============================================
-- 执行完成后，使用以下信息登录：
-- 用户名: root
-- 密码: 111111
-- =============================================
