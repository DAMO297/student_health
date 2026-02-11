package org.example.analysis;

import org.example.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/overview")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> getOverview() {
        return ApiResponse.ok(analysisService.getOverview());
    }

    @GetMapping("/activity")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Map<String, Object>>> getActivity() {
        return ApiResponse.ok(analysisService.getActivityTrend());
    }

    @GetMapping("/college-stats")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Map<String, Object>>> getCollegeStats() {
        return ApiResponse.ok(analysisService.getCollegeStats());
    }

    @GetMapping("/gender-stats")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Map<String, Object>>> getGenderStats() {
        return ApiResponse.ok(analysisService.getGenderStats());
    }

    @GetMapping("/grade-stats")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Map<String, Object>>> getGradeStats() {
        return ApiResponse.ok(analysisService.getGradeStats());
    }

    @GetMapping("/trend-stats")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Map<String, Object>>> getTrendStats() {
        return ApiResponse.ok(analysisService.getTrendStats());
    }

    @GetMapping("/risk-clusters")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Map<String, Object>>> getRiskClusters() {
        return ApiResponse.ok(analysisService.getRiskClusters());
    }

    @GetMapping("/student-trend")
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ApiResponse<Map<String, Object>> getStudentTrend(
            @org.springframework.web.bind.annotation.RequestParam("studentId") Long studentId,
            @org.springframework.web.bind.annotation.RequestParam("metricKey") String metricKey) {
        return ApiResponse.ok(analysisService.predictStudentTrend(studentId, metricKey));
    }
}
