package org.example.user;

import org.example.auth.dto.RegisterRequest;
import org.example.common.BizException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity getById(Long id) {
        return userMapper.selectById(id);
    }

    public UserEntity getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 返回 Spring Security authorities：包含 ROLE_* + 权限点 code
     */
    public List<String> getAuthorities(Long userId) {
        Set<String> set = new LinkedHashSet<>();
        List<String> roles = userMapper.selectRoleCodesByUserId(userId);
        if (roles != null)
            set.addAll(roles);
        List<String> perms = userMapper.selectPermissionCodesByUserId(userId);
        if (perms != null)
            set.addAll(perms);
        return new ArrayList<>(set);
    }

    public void touchLogin(Long userId) {
        userMapper.updateLastLoginAt(userId);
    }

    @Transactional
    public void register(RegisterRequest req) {
        if (userMapper.selectByUsername(req.getUsername()) != null) {
            throw new BizException(400, "用户名已存在");
        }

        UserEntity user = new UserEntity();
        user.setUsername(req.getUsername());
        user.setDisplayName(req.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        Integer type = req.getUserType() != null ? req.getUserType() : 3;
        user.setUserType(type);

        // 医生(2)默认待审核(0)，学生(3)默认正常(1)
        user.setStatus(type == 2 ? 0 : 1);

        user.setTokenVersion(0);
        user.setCreatedBy("system");
        user.setUpdatedBy("system");

        userMapper.insert(user);

        // Assign Role
        String roleCode = (type == 2) ? "ROLE_DOCTOR" : "ROLE_STUDENT";
        Long roleId = userMapper.selectRoleIdByCode(roleCode);
        if (roleId != null) {
            userMapper.insertUserRole(user.getId(), roleId);
        }
    }

    /**
     * 根据学生ID获取用户账号
     */
    public UserEntity getUserByStudentId(Long studentId) {
        return userMapper.selectByStudentId(studentId);
    }

    /**
     * 为学生创建用户账号
     */
    @Transactional
    public UserEntity createStudentAccount(Long studentId, String password, String operator) {
        // 检查是否已存在账号
        UserEntity existing = userMapper.selectByStudentId(studentId);
        if (existing != null) {
            throw new BizException(400, "该学生已绑定账号");
        }

        // 需要获取学生信息来设置用户名和显示名
        // 这里需要注入 StudentService 或 StudentMapper
        // 暂时简化处理，由调用方传入学生信息
        throw new BizException(500, "需要学生信息来创建账号，请使用 createStudentAccountWithInfo 方法");
    }

    /**
     * 为学生创建用户账号（带学生信息）
     */
    @Transactional
    public UserEntity createStudentAccountWithInfo(Long studentId, String studentNo, String studentName,
            String password, String operator) {
        // 检查是否已存在账号
        UserEntity existing = userMapper.selectByStudentId(studentId);
        if (existing != null) {
            throw new BizException(400, "该学生已绑定账号");
        }

        // 检查用户名是否已被占用
        if (userMapper.selectByUsername(studentNo) != null) {
            throw new BizException(400, "该学号已被其他用户使用");
        }

        // 创建用户账号
        UserEntity user = new UserEntity();
        user.setUsername(studentNo);
        user.setDisplayName(studentName);
        user.setUserType(3); // 学生类型
        user.setStudentId(studentId);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus(1); // 正常状态
        user.setTokenVersion(0);
        user.setCreatedBy(operator);
        user.setUpdatedBy(operator);

        userMapper.insert(user);

        // 分配学生角色
        Long roleId = userMapper.selectRoleIdByCode("ROLE_STUDENT");
        if (roleId != null) {
            userMapper.insertUserRole(user.getId(), roleId);
        }

        return user;
    }

    /**
     * 重置学生账号密码
     */
    @Transactional
    public void resetStudentPassword(Long studentId, String newPassword) {
        UserEntity user = userMapper.selectByStudentId(studentId);
        if (user == null) {
            throw new BizException(404, "该学生未绑定账号");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(user.getId(), hashedPassword);
    }

    /**
     * 更新用户显示名称
     */
    @Transactional
    public void updateDisplayName(Long userId, String displayName) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }

        user.setDisplayName(displayName);
        user.setUpdatedBy(user.getUsername());
        userMapper.updateDisplayName(userId, displayName);
    }

    /**
     * 修改用户密码（验证旧密码）
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BizException(400, "旧密码错误");
        }

        // 更新密码
        String hashedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(user.getId(), hashedPassword);
    }
}
