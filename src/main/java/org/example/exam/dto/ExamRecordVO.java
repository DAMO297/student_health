package org.example.exam.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 体检记录列表展示 VO
 * 包含关联的学生和医生信息
 */
@Data
public class ExamRecordVO {
    private Long id;
    private Long batchId;
    private Long studentId;
    private Long doctorId;
    private LocalDateTime recordTime;
    private Integer source;
    private Integer auditStatus;
    private Integer abnormalFlag;
    private String remark;

    // 关联的学生信息
    private String studentNo;
    private String studentName;

    // 关联的医生信息
    private String doctorName;

    // 体检指标（从 t_exam_detail 表获取）
    private Double height;
    private Double weight;
    private Double bmi;
    private Integer sbp; // 收缩压
    private Integer dbp; // 舒张压
    private Integer heartRate;
    private Double visionL;
    private Double visionR;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
