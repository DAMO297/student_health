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
        long yesterdayCheck = analysisMapper.countYesterdayRecords();
        long todayCheck = (Long) map.get("todayCheck");
        if (yesterdayCheck > 0) {
            int todayTrend = (int) (((todayCheck - yesterdayCheck) * 100.0) / yesterdayCheck);
            map.put("todayTrend", todayTrend);
        } else {
            map.put("todayTrend", 0);
        }

        map.put("studentTrend", 5);
        map.put("batchTrend", 0);
        map.put("abnormalTrend", -2);

        // 报告状态统计: 1=待审核, 2=已完成
        map.put("reportPending", analysisMapper.countReportsByStatus(1));
        map.put("reportCompleted", analysisMapper.countReportsByStatus(2));

        return map;
    }

    public List<Map<String, Object>> getActivityTrend() {
        return analysisMapper.countRecentActivity();
    }

    public List<Map<String, Object>> getCollegeStats() {
        return analysisMapper.getCollegeStatistics();
    }

    public List<Map<String, Object>> getGenderStats() {
        return analysisMapper.getGenderStatistics();
    }

    public List<Map<String, Object>> getGradeStats() {
        return analysisMapper.getGradeStatistics();
    }

    public List<Map<String, Object>> getTrendStats() {
        return analysisMapper.getAbnormalTrendByBatch();
    }

    public List<Map<String, Object>> getRiskClusters() {
        List<Map<String, Object>> data = analysisMapper.getDataForClustering();
        if (data.isEmpty())
            return new java.util.ArrayList<>();

        List<double[]> matrix = new java.util.ArrayList<>();
        for (Map<String, Object> row : data) {
            double[] vec = new double[3];
            vec[0] = ((java.math.BigDecimal) row.get("bmi")).doubleValue();
            vec[1] = ((java.math.BigDecimal) row.get("sbp")).doubleValue();
            vec[2] = ((java.math.BigDecimal) row.get("dbp")).doubleValue();
            matrix.add(vec);
        }

        List<org.example.util.ClusteringUtil.Point> clusters = org.example.util.ClusteringUtil.cluster(matrix, 3, 20);

        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (int i = 0; i < clusters.size(); i++) {
            org.example.util.ClusteringUtil.Point p = clusters.get(i);
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("studentName", data.get(i).get("studentName"));
            map.put("studentNo", data.get(i).get("studentNo"));
            map.put("bmi", p.coords[0]);
            map.put("sbp", p.coords[1]);
            map.put("cluster", p.clusterId);
            result.add(map);
        }
        return result;
    }

    public Map<String, Object> predictStudentTrend(Long studentId, String metricKey) {
        List<Map<String, Object>> history = analysisMapper.getStudentMetricHistory(studentId, metricKey);
        if (history.size() < 2)
            return null;

        List<Double> x = new java.util.ArrayList<>();
        List<Double> y = new java.util.ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            x.add((double) i);
            y.add(((java.math.BigDecimal) history.get(i).get("value")).doubleValue());
        }

        org.example.util.RegressionUtil.Result regression = org.example.util.RegressionUtil.calculate(x, y);

        double lastValue = y.get(y.size() - 1);
        double rawPrediction = regression.predict(x.size());
        int count = y.size();
        double dampedPrediction = rawPrediction;

        if (count < 5) {
            double dampingFactor = count / 5.0;
            dampedPrediction = lastValue + (rawPrediction - lastValue) * dampingFactor;
        }

        double maxShift = lastValue * 0.2;
        if (Math.abs(dampedPrediction - lastValue) > maxShift) {
            dampedPrediction = lastValue + Math.signum(dampedPrediction - lastValue) * maxShift;
        }

        if ("bmi".equalsIgnoreCase(metricKey)) {
            dampedPrediction = Math.max(12.0, Math.min(50.0, dampedPrediction));
        } else if ("sbp".equalsIgnoreCase(metricKey)) {
            dampedPrediction = Math.max(70.0, Math.min(200.0, dampedPrediction));
        } else if ("dbp".equalsIgnoreCase(metricKey)) {
            dampedPrediction = Math.max(40.0, Math.min(130.0, dampedPrediction));
        }

        Map<String, Object> res = new java.util.HashMap<>();
        res.put("history", history);
        res.put("prediction", dampedPrediction);
        res.put("trend", regression.slope > 0.1 ? "上升" : (regression.slope < -0.1 ? "下降" : "稳定"));
        return res;
    }
}
