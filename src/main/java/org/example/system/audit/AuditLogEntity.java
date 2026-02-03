package org.example.system.audit;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLogEntity {
    private Long id;
    private Long userId;
    private String roleCode;
    private String action;
    private String resource;
    private String resourceId;
    private String detail;
    private Integer result; // 1 success, 2 fail
    private Integer costMs;
    private String ip;
    private String userAgent;
    private String traceId;
    private LocalDateTime createdAt;
}
