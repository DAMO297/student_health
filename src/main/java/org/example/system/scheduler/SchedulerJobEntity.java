package org.example.system.scheduler;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SchedulerJobEntity {
    private Long id;
    private String jobName;
    private String jobGroup;
    private String beanName; // Spring Bean Name
    private String methodName;
    private String methodParams;
    private String cronExpression;
    private Integer status; // 0 normal, 1 paused
    private Integer concurrent; // 0 allow, 1 forbid
    private String remark;

    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
