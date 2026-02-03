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
}

