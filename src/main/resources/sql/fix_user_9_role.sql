-- =============================================
-- 紧急修复：为用户ID 9 分配管理员角色
-- =============================================

-- 1. 检查用户ID 9 的信息
SELECT id, username, display_name, user_type, status FROM t_user WHERE id = 9;

-- 2. 检查该用户当前的角色
SELECT ur.*, r.code, r.name 
FROM t_user_role ur 
JOIN t_role r ON ur.role_id = r.id 
WHERE ur.user_id = 9;

-- 3. 删除该用户的旧角色（如果有）
DELETE FROM t_user_role WHERE user_id = 9;

-- 4. 为该用户分配 ROLE_ADMIN 角色
INSERT INTO t_user_role (user_id, role_id)
SELECT 9, id FROM t_role WHERE code = 'ROLE_ADMIN';

-- 5. 验证修复结果
SELECT 
    u.id,
    u.username,
    r.code as role_code,
    r.name as role_name
FROM t_user u
JOIN t_user_role ur ON u.id = ur.user_id
JOIN t_role r ON ur.role_id = r.id
WHERE u.id = 9;

-- 6. 查看该用户的所有权限
SELECT p.code, p.name
FROM t_user u
JOIN t_user_role ur ON u.id = ur.user_id
JOIN t_role_permission rp ON ur.role_id = rp.role_id
JOIN t_permission p ON rp.permission_id = p.id
WHERE u.id = 9;

-- =============================================
-- 执行完成后：
-- 1. 完全退出登录
-- 2. 清除浏览器缓存（Ctrl+Shift+Delete）
-- 3. 重新登录
-- =============================================
