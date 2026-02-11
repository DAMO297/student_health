package org.example.exam.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ExamRecordExcelModel {

    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("体检日期")
    private String recordTime; // yyyy-MM-dd HH:mm:ss

    @ExcelProperty("身高(cm)")
    private BigDecimal height;

    @ExcelProperty("体重(kg)")
    private BigDecimal weight;

    @ExcelProperty("收缩压(mmHg)")
    private BigDecimal sbp;

    @ExcelProperty("舒张压(mmHg)")
    private BigDecimal dbp;

    @ExcelProperty("左眼视力")
    private BigDecimal visionLeft;

    @ExcelProperty("右眼视力")
    private BigDecimal visionRight;

    @ExcelProperty("备注")
    private String remark;
}
