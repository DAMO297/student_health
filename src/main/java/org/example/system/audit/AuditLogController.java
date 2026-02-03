package org.example.system.audit;

import org.example.common.ApiResponse;
import org.example.common.PageResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditLogMapper auditLogMapper;

    public AuditLogController(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<AuditLogEntity>> list(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resource,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {

        int offset = (page - 1) * pageSize;
        List<AuditLogEntity> list = auditLogMapper.selectList(username, action, resource, offset, pageSize);
        int total = auditLogMapper.count(username, action, resource);

        return ApiResponse.ok(new PageResult<>(total, list));
    }
}
