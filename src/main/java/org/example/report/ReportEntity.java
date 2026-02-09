package org.example.report;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportEntity {
    private Long id;
    private Long recordId;
    private String reportNo;
    private Integer version;
    private Integer status; // 1草稿 2已生成 3已归档
    private String summary;
    private String doctorAdvice;
    private LocalDateTime generatedAt;
    private LocalDateTime archivedAt;
    private Integer deleted;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    // 关联字段（用于列表展示）
    private String studentName;
    private String studentNo;
    private String batchName;
}
