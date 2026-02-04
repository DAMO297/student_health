package org.example.student.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建学生账号请求
 */
@Data
public class CreateStudentAccountRequest {
    @NotBlank(message = "密码不能为空")
    private String password;
}
