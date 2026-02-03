package org.example.exam.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExamMetricSaveRequest {
    private String metricKey;
    private String metricName;
    private BigDecimal valueDecimal;
    private String valueText;
    private String unit;
    private BigDecimal refLow;
    private BigDecimal refHigh;
    private Integer abnormalFlag;
}





