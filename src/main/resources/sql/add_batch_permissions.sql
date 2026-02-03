-- =============================================
-- 体检批次管理权限配置
-- =============================================

-- 添加体检批次管理权限
INSERT IGNORE INTO t_permission (code, name, description, created_at, updated_at)
VALUES 
    ('exam_batch:read', '查看体检批次', '查看体检批次信息', NOW(), NOW()),
    ('exam_batch:create', '创建体检批次', '创建新的体检批次', NOW(), NOW()),
    ('exam_batch:update', '更新体检批次', '更新体检批次信息', NOW(), NOW()),
    ('exam_batch:delete', '删除体检批次', '删除体检批次', NOW(), NOW());

-- 为管理员角色分配批次管理权限
INSERT IGNORE INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM t_role r, t_permission p
WHERE r.code = 'ROLE_ADMIN'
AND p.code IN ('exam_batch:read', 'exam_batch:create', 'exam_batch:update', 'exam_batch:delete');

-- 验证权限
SELECT '=== 体检批次管理权限 ===' as info;
SELECT p.code, p.name
FROM t_permission p
WHERE p.code LIKE 'exam_batch:%';

SELECT '=== 管理员拥有的批次权限 ===' as info;
SELECT p.code, p.name
FROM t_role r
JOIN t_role_permission rp ON r.id = rp.role_id
JOIN t_permission p ON rp.permission_id = p.id
WHERE r.code = 'ROLE_ADMIN'
AND p.code LIKE 'exam_batch:%';
