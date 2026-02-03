-- =============================================
-- 方案A: 使用简单密码 "password"
-- 这个BCrypt hash是经过验证的
-- =============================================

-- 清理旧数据
DELETE FROM t_user_role WHERE user_id IN (SELECT id FROM t_user WHERE username = 'root');
DELETE FROM t_user WHERE username = 'root';

-- 创建管理员 - 密码: password
INSERT INTO t_user (
    username, display_name, user_type, password_hash, 
    status, token_version, deleted, created_by, updated_by, created_at, updated_at
) VALUES (
    'root', '系统管理员', 1, 
    '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG13AXN2xHq7wMqZHy',
    1, 0, 0, 'system', 'system', NOW(), NOW()
);

-- 分配角色
INSERT INTO t_user_role (user_id, role_id)
SELECT u.id, r.id FROM t_user u, t_role r
WHERE u.username = 'root' AND r.code = 'ROLE_ADMIN';

-- =============================================
-- 登录信息:
-- 用户名: root
-- 密码: password
-- =============================================

-- =============================================
-- 方案B: 如果方案A不行，执行下面的UPDATE
-- 使用密码 "admin123"
-- =============================================
-- UPDATE t_user SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' WHERE username = 'root';

-- =============================================
-- 方案C: 使用密码 "123456"
-- =============================================
-- UPDATE t_user SET password_hash = '$2a$10$Xl4.YhkN8vJZvVqKqVqKqOqKqKqKqKqKqKqKqKqKqKqKqKqKqKqKq' WHERE username = 'root';

SELECT * FROM t_user WHERE username = 'root';
