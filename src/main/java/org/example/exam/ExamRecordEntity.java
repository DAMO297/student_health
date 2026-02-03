package org.example.exam;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamRecordEntity {
    private Long id;
    private Long batchId;
    private Long studentId;
    private Long doctorId;
    private LocalDateTime recordTime;
    private Integer source; // 1手工 2批量导入
    private Integer auditStatus; // 1待审核 2已审核 3驳回
    private Integer abnormalFlag; // 0否 1是
    private String remark;
    private Integer version;
    private Integer deleted;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}





