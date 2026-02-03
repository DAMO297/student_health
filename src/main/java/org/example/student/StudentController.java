package org.example.student;

import org.example.common.ApiResponse;
import org.example.common.PageResult;
import org.example.common.audit.AuditLog;
import org.example.security.SecurityUtil;
import org.example.student.dto.StudentSaveRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.io.IOException;
import com.alibaba.excel.EasyExcel;
import org.example.student.dto.StudentExcelModel;
import org.example.student.dto.StudentImportResult;
import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('student:read')")
    public ApiResponse<PageResult<StudentEntity>> page(
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String clazz,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(studentService.page(studentNo, name, college, grade, clazz, status, page, pageSize));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('student:read')")
    public ApiResponse<StudentEntity> get(@PathVariable Long id) {
        return ApiResponse.ok(studentService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('student:create')")
    @AuditLog(action = "新增学生", resource = "student")
    public ApiResponse<StudentEntity> create(@Validated @RequestBody StudentSaveRequest req) {
        return ApiResponse.ok(studentService.create(req, SecurityUtil.currentUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('student:update')")
    @AuditLog(action = "更新学生", resource = "student")
    public ApiResponse<StudentEntity> update(@PathVariable Long id, @Validated @RequestBody StudentSaveRequest req) {
        return ApiResponse.ok(studentService.update(id, req, SecurityUtil.currentUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('student:delete')")
    @AuditLog(action = "删除学生", resource = "student")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('student:import')")
    @AuditLog(action = "导入学生", resource = "student")
    public ApiResponse<StudentImportResult> importStudents(@RequestParam("file") MultipartFile file)
            throws IOException {
        return ApiResponse.ok(studentService.importStudents(file.getInputStream(), SecurityUtil.currentUsername()));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('student:export')")
    public void export(
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String clazz,
            @RequestParam(required = false) Integer status,
            HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("student_export_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+",
                "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        List<StudentExcelModel> list = studentService.exportStudents(studentNo, name, college, grade, clazz, status);
        EasyExcel.write(response.getOutputStream(), StudentExcelModel.class).sheet("学生数据").doWrite(list);
    }
}

