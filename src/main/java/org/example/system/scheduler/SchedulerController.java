package org.example.system.scheduler;

import org.example.common.ApiResponse;
import org.example.common.audit.AuditLog;
import org.example.security.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {

    private final SchedulerService schedulerService;

    public SchedulerController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:scheduler:read')")
    public ApiResponse<List<SchedulerJobEntity>> list(
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.ok(schedulerService.list(jobName, status));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:scheduler:create')")
    @AuditLog(action = "创建任务", resource = "scheduler_job")
    public ApiResponse<SchedulerJobEntity> create(@RequestBody SchedulerJobEntity req) {
        return ApiResponse.ok(schedulerService.create(req, SecurityUtil.currentUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:scheduler:update')")
    @AuditLog(action = "更新任务", resource = "scheduler_job")
    public ApiResponse<SchedulerJobEntity> update(@PathVariable Long id, @RequestBody SchedulerJobEntity req) {
        return ApiResponse.ok(schedulerService.update(id, req, SecurityUtil.currentUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:scheduler:delete')")
    @AuditLog(action = "删除任务", resource = "scheduler_job")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        schedulerService.delete(id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:scheduler:update')")
    @AuditLog(action = "更新任务状态", resource = "scheduler_job")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        schedulerService.updateStatus(id, status);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/run")
    @PreAuthorize("hasAuthority('system:scheduler:run')")
    @AuditLog(action = "执行任务", resource = "scheduler_job")
    public ApiResponse<Void> run(@PathVariable Long id) {
        schedulerService.run(id);
        return ApiResponse.ok(null);
    }
}
