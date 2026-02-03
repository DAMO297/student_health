-- =============================================
-- 最简单的解决方案：直接检查和修复
-- =============================================

-- 1. 检查root用户是否存在及其角色
SELECT 
    u.id,
    u.username,
    u.user_type,
    u.status,
    r.code as role_code
FROM t_user u
LEFT JOIN t_user_role ur ON u.id = ur.user_id
LEFT JOIN t_role r ON ur.role_id = r.id
WHERE u.username = 'root';

-- 2. 如果上面显示没有角色，执行下面的修复
-- 确保ROLE_ADMIN存在
INSERT IGNORE INTO t_role (code, name, description, created_at, updated_at)
VALUES ('ROLE_ADMIN', '管理员', '系统管理员角色', NOW(), NOW());

-- 为root用户分配ROLE_ADMIN
DELETE FROM t_user_role WHERE user_id = (SELECT id FROM t_user WHERE username = 'root');
INSERT INTO t_user_role (user_id, role_id)
SELECT u.id, r.id
FROM t_user u, t_role r
WHERE u.username = 'root' AND r.code = 'ROLE_ADMIN';

-- 3. 验证修复结果
SELECT 
    u.username,
    u.user_type,
    r.code as role_code,
    r.name as role_name
FROM t_user u
JOIN t_user_role ur ON u.id = ur.user_id
JOIN t_role r ON ur.role_id = r.id
WHERE u.username = 'root';

-- =============================================
-- 执行完后，请：
-- 1. 确认后端已重启（检查控制台是否有启动日志）
-- 2. 完全退出登录
-- 3. 清除浏览器所有缓存（Ctrl+Shift+Delete）
-- 4. 关闭浏览器重新打开
-- 5. 重新登录
-- =============================================
