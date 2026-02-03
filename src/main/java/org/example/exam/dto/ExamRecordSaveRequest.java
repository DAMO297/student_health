package org.example.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ExamRecordSaveRequest {
    @NotNull
    private Long batchId;
    @NotNull
    private Long studentId;

    private String recordTime; // yyyy-MM-dd HH:mm:ss（为空则NOW）
    private Integer source; // 1手工 2批量导入（默认1）
    private Integer auditStatus; // 默认1
    private String remark;

    private List<ExamMetricSaveRequest> metrics;
}





