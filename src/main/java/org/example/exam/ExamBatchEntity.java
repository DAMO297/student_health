package org.example.exam;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ExamBatchEntity {
    private Long id;
    private String batchName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status; // 1未开始 2进行中 3已结束 4归档
    private String remark;
    private Integer deleted;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}





