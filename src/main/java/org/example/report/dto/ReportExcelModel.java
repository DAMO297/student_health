package org.example.report.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.util.Date;

@Data
public class ReportExcelModel {

    @ExcelProperty("报告编号")
    private String reportNo;

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("学院")
    private String college;

    @ExcelProperty("体检批次")
    private String batchName;

    @ExcelProperty("综评摘要")
    private String summary;

    @ExcelProperty("医师建议")
    private String doctorAdvice;

    @ExcelProperty("生成时间")
    private Date generatedAt; // EasyExcel handles Date automatically
}
