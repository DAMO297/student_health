package org.example.user;

import org.example.common.ApiResponse;
import org.example.security.SecurityUtil;
import org.example.student.StudentEntity;
import org.example.student.StudentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final StudentService studentService;

    public UserController(UserService userService, StudentService studentService) {
        this.userService = userService;
        this.studentService = studentService;
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> getProfile() {
        Long userId = SecurityUtil.currentUserId();
        UserEntity user = userService.getById(userId);

        Map<String, Object> profile = new HashMap<>();
        profile.put("user", user);

        if (user.getStudentId() != null) {
            StudentEntity student = studentService.get(user.getStudentId());
            profile.put("student", student);
        }

        return ApiResponse.ok(profile);
    }
}
