package org.example.exam;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExamMetricEntity {
    private Long id;
    private Long recordId;
    private String metricKey;
    private String metricName;
    private BigDecimal valueDecimal;
    private String valueText;
    private String unit;
    private BigDecimal refLow;
    private BigDecimal refHigh;
    private Integer abnormalFlag; // 0正常 1偏低 2偏高 3异常文本
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}





