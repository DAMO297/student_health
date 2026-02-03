package org.example.user;

import org.example.common.ApiResponse;
import org.example.common.audit.AuditLog;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserAuditController {

    private final UserMapper userMapper;

    public UserAuditController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserEntity>> getPendingUsers() {
        List<UserEntity> users = userMapper.selectByStatus(0);
        return ApiResponse.ok(users);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditLog(action = "审核通过用户", resource = "user")
    public ApiResponse<Void> approveUser(@PathVariable Long id) {
        userMapper.updateStatus(id, 1);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditLog(action = "拒绝用户注册", resource = "user")
    public ApiResponse<Void> rejectUser(@PathVariable Long id) {
        userMapper.updateStatus(id, 2);
        return ApiResponse.ok(null);
    }
}
