package org.example.student;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudentEntity {
    private Long id;
    private String studentNo;
    private String name;
    private Integer gender; // 0未知 1男 2女
    private String college;
    private String grade;
    private String clazz;
    private String idCard;
    private String phone;
    private String email;
    private LocalDate birthday;
    private Integer status; // 1正常 2冻结 3毕业 4删除
    private Integer version;
    private Integer deleted; // 0否 1是
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}





