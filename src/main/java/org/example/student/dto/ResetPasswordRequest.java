package org.example.student.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 重置学生账号密码请求
 */
@Data
public class ResetPasswordRequest {
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
