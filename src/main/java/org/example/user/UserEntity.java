package org.example.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserEntity {
    private Long id;
    private String username;
    private String displayName;
    private Integer userType; // 1管理员 2医生 3学生
    private Long studentId;
    private String passwordHash;
    private Integer status; // 1正常 2冻结
    private Integer tokenVersion;
    private LocalDateTime lastLoginAt;
    private Integer deleted; // 0否 1是
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}





