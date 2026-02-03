package org.example.system.dict;

import org.example.common.ApiResponse;
import org.example.common.audit.AuditLog;
import org.example.security.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dict")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping
    public ApiResponse<List<DictionaryEntity>> list(
            @RequestParam(required = false) String typeCode,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.ok(dictionaryService.list(typeCode, status));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:dict:create')")
    @AuditLog(action = "新增字典", resource = "dict")
    public ApiResponse<DictionaryEntity> create(@RequestBody DictionaryEntity req) {
        return ApiResponse.ok(dictionaryService.create(req, SecurityUtil.currentUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dict:update')")
    @AuditLog(action = "更新字典", resource = "dict")
    public ApiResponse<DictionaryEntity> update(@PathVariable Long id, @RequestBody DictionaryEntity req) {
        return ApiResponse.ok(dictionaryService.update(id, req, SecurityUtil.currentUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dict:delete')")
    @AuditLog(action = "删除字典", resource = "dict")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dictionaryService.delete(id);
        return ApiResponse.ok(null);
    }
}
