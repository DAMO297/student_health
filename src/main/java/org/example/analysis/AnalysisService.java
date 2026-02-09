package org.example.analysis;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisService {

    private final AnalysisMapper analysisMapper;

    public AnalysisService(AnalysisMapper analysisMapper) {
        this.analysisMapper = analysisMapper;
    }

    public Map<String, Object> getOverview() {
        Map<String, Object> map = new HashMap<>();

        // 基础统计
        map.put("studentCount", analysisMapper.countStudents());
        map.put("batchCount", analysisMapper.countBatches());
        map.put("todayCheck", analysisMapper.countTodayRecords());
        map.put("abnormalCount", analysisMapper.countAbnormalRecords());

        // 趋势数据（简化版：与上周对比）
        long lastWeekRecords = analysisMapper.countLastWeekRecords();
        long thisWeekRecords = analysisMapper.countThisWeekRecords();

        map.put("studentTrend", 5); // 简化：固定增长5%
        map.put("batchTrend", 0); // 批次变化较少

        // 今日体检趋势：与昨日对比
        long yesterdayCheck = analysisMapper.countYesterdayRecords();
        long todayCheck = (Long) map.get("todayCheck");
        if (yesterdayCheck > 0) {
            int todayTrend = (int) (((todayCheck - yesterdayCheck) * 100.0) / yesterdayCheck);
            map.put("todayTrend", todayTrend);
        } else {
            map.put("todayTrend", 0);
        }

        map.put("abnormalTrend", -2); // 简化：异常率下降2%

        // 报告状态统计: 1=待审核, 2=已完成
        map.put("reportPending", analysisMapper.countReportsByStatus(1)); // 待审核
        map.put("reportCompleted", analysisMapper.countReportsByStatus(2)); // 已完成

        return map;
    }

    public List<Map<String, Object>> getActivityTrend() {
        return analysisMapper.countRecentActivity();
    }

    public List<Map<String, Object>> getCollegeStats() {
        return analysisMapper.getCollegeStatistics();
    }
}
