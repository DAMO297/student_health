package org.example.student.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class StudentExcelModel {

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty(value = "性别")
    private String gender; // 接收 "男"/"女"

    @ExcelProperty("学院")
    private String college;

    @ExcelProperty("年级")
    private String grade;

    @ExcelProperty("班级")
    private String clazz;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;
}
