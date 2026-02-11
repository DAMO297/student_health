package org.example.exam;

import org.example.common.ApiResponse;
import org.example.common.PageResult;
import org.example.common.audit.AuditLog;
import org.example.exam.dto.ExamRecordSaveRequest;
import org.example.security.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ExamController {

    private final ExamService examService;
    private final org.example.user.UserService userService;

    public ExamController(ExamService examService, org.example.user.UserService userService) {
        this.examService = examService;
        this.userService = userService;
    }

    // 体检批次
    @GetMapping("/exam-batches")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ExamBatchEntity>> listBatches(@RequestParam(required = false) Integer status) {
        return ApiResponse.ok(examService.listBatches(status));
    }

    @PostMapping("/exam-batches")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditLog(action = "创建体检批次", resource = "exam_batch")
    public ApiResponse<ExamBatchEntity> createBatch(@RequestBody ExamBatchEntity req) {
        return ApiResponse.ok(examService.createBatch(req, SecurityUtil.currentUsername()));
    }

    @PutMapping("/exam-batches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditLog(action = "更新体检批次", resource = "exam_batch")
    public ApiResponse<ExamBatchEntity> updateBatch(@PathVariable Long id, @RequestBody ExamBatchEntity req) {
        return ApiResponse.ok(examService.updateBatch(id, req, SecurityUtil.currentUsername()));
    }

    @DeleteMapping("/exam-batches/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditLog(action = "删除体检批次", resource = "exam_batch")
    public ApiResponse<Void> deleteBatch(@PathVariable Long id) {
        examService.deleteBatch(id);
        return ApiResponse.ok(null);
    }

    // 体检记录
    @GetMapping("/exam-records")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResult<org.example.exam.dto.ExamRecordVO>> pageRecords(
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Integer abnormalFlag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        Long userId = SecurityUtil.currentUserId();
        org.example.user.UserEntity user = userService.getById(userId);
        Long studentIdFilter = studentId;
        if (user != null && user.getUserType() == 3) {
            studentIdFilter = user.getStudentId();
            if (studentIdFilter == null) {
                return ApiResponse.ok(new PageResult<>(0, java.util.Collections.emptyList()));
            }
        }

        return ApiResponse
                .ok(examService.pageRecords(batchId, studentIdFilter, doctorId, abnormalFlag, page, pageSize));
    }

    @GetMapping("/exam-records/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ExamRecordEntity> getRecord(@PathVariable Long id) {
        // Simple security: for now we allow authenticated, but ideally we check
        // studentId match
        return ApiResponse.ok(examService.getRecord(id));
    }

    @GetMapping("/exam-records/{id}/metrics")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ExamMetricEntity>> listMetrics(@PathVariable Long id) {
        return ApiResponse.ok(examService.listMetrics(id));
    }

    @PostMapping("/exam-records")
    @PreAuthorize("hasAuthority('exam_record:create')")
    @AuditLog(action = "创建体检记录", resource = "exam_record")
    public ApiResponse<ExamRecordEntity> createRecord(@Validated @RequestBody ExamRecordSaveRequest req) {
        return ApiResponse.ok(examService.createRecord(req, SecurityUtil.currentUsername()));
    }

    @PutMapping("/exam-records/{id}")
    @PreAuthorize("hasAuthority('exam_record:update')")
    @AuditLog(action = "更新体检记录", resource = "exam_record")
    public ApiResponse<ExamRecordEntity> updateRecord(@PathVariable Long id,
            @Validated @RequestBody ExamRecordSaveRequest req) {
        return ApiResponse.ok(examService.updateRecord(id, req, SecurityUtil.currentUsername()));
    }

    @DeleteMapping("/exam-records/{id}")
    @PreAuthorize("hasAuthority('exam_record:delete')")
    @AuditLog(action = "删除体检记录", resource = "exam")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        examService.deleteRecord(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/exam-records/import")
    @PreAuthorize("hasAuthority('student:import')") // Consistently use import permission
    @AuditLog(action = "批量导入体检记录", resource = "exam")
    public ApiResponse<org.example.common.dto.ImportResult> importRecords(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("batchId") Long batchId) throws java.io.IOException {
        return ApiResponse
                .ok(examService.importRecords(file.getInputStream(), batchId, SecurityUtil.currentUsername()));
    }
}
