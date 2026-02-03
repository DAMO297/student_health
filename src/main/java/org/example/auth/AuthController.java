package org.example.auth;

import org.example.auth.dto.LoginRequest;
import org.example.auth.dto.LoginResponse;
import org.example.common.ApiResponse;
import org.example.common.BizException;
import org.example.security.JwtProperties;
import org.example.security.JwtUtil;
import org.example.user.UserEntity;
import org.example.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
            JwtProperties jwtProperties) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Validated @RequestBody LoginRequest req) {
        UserEntity user = userService.getByUsername(req.getUsername());
        if (user == null || user.getDeleted() != 0) {
            throw new BizException(40101, "账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(40101, "账号已冻结");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BizException(40101, "账号或密码错误");
        }

        List<String> authorities = userService.getAuthorities(user.getId());
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), authorities,
                user.getTokenVersion() == null ? 0 : user.getTokenVersion());
        userService.touchLogin(user.getId());

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getUserType(),
                authorities);
        return ApiResponse.ok(new LoginResponse(token, jwtProperties.getExpireSeconds(), userInfo));
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Validated @RequestBody org.example.auth.dto.RegisterRequest req) {
        userService.register(req);
        return ApiResponse.ok(null);
    }
}

