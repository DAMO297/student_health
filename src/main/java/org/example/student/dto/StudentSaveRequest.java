package org.example.student.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class StudentSaveRequest {
    @NotBlank
    private String studentNo;
    @NotBlank
    private String name;
    @NotNull
    private Integer gender;
    @NotBlank
    private String college;
    @NotBlank
    private String grade;
    @NotBlank
    private String clazz;

    private String idCard;
    private String phone;
    private String email;
    private String birthday; // yyyy-MM-dd（简化，后端解析时可再增强）
    private Integer status; // 默认1
}





