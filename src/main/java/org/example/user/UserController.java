package org.example.user;

import org.example.common.ApiResponse;
import org.example.common.audit.AuditLog;
import org.example.security.SecurityUtil;
import org.example.user.dto.ChangePasswordRequest;
import org.example.user.dto.UpdateProfileRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户个人信息管理控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前用户个人信息
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserEntity> getProfile() {
        Long userId = SecurityUtil.currentUserId();
        UserEntity user = userService.getById(userId);
        // 清除密码字段
        if (user != null) {
            user.setPasswordHash(null);
        }
        return ApiResponse.ok(user);
    }

    /**
     * 更新个人资料
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @AuditLog(action = "更新个人资料", resource = "user_profile")
    public ApiResponse<Void> updateProfile(@Validated @RequestBody UpdateProfileRequest req) {
        Long userId = SecurityUtil.currentUserId();
        userService.updateDisplayName(userId, req.getDisplayName());
        return ApiResponse.ok(null);
    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @AuditLog(action = "修改密码", resource = "user_password")
    public ApiResponse<Void> changePassword(@Validated @RequestBody ChangePasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            return ApiResponse.fail(400, "两次密码输入不一致");
        }

        if (req.getNewPassword().length() < 6) {
            return ApiResponse.fail(400, "密码长度至少6位");
        }

        Long userId = SecurityUtil.currentUserId();
        userService.changePassword(userId, req.getOldPassword(), req.getNewPassword());
        return ApiResponse.ok(null);
    }
}
