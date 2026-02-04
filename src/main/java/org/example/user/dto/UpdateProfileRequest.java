package org.example.user.dto;

import lombok.Data;

/**
 * 更新个人资料请求
 */
@Data
public class UpdateProfileRequest {
    private String displayName;
}
