package org.example.analysis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface AnalysisMapper {
        @Select("SELECT COUNT(1) FROM t_student WHERE deleted = 0")
        long countStudents();

        @Select("SELECT COUNT(1) FROM t_exam_batch WHERE deleted = 0")
        long countBatches();

        @Select("SELECT COUNT(1) FROM t_exam_record WHERE deleted = 0 AND DATE(updated_at) = CURDATE()")
        long countTodayRecords();

        @Select("SELECT COUNT(1) FROM t_exam_record WHERE deleted = 0 AND DATE(updated_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
        long countYesterdayRecords();

        @Select("SELECT COUNT(1) FROM t_exam_record WHERE deleted = 0 AND updated_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)")
        long countLastWeekRecords();

        @Select("SELECT COUNT(1) FROM t_exam_record WHERE deleted = 0 AND updated_at >= DATE_SUB(CURDATE(), INTERVAL 14 DAY) AND updated_at < DATE_SUB(CURDATE(), INTERVAL 7 DAY)")
        long countThisWeekRecords();

        @Select("SELECT COUNT(1) FROM t_exam_record WHERE deleted = 0 AND abnormal_flag = 1")
        long countAbnormalRecords();

        @Select("SELECT COUNT(1) FROM t_report WHERE deleted = 0 AND status = #{status}")
        long countReportsByStatus(int status);

        // Recent 7 days activity
        @Select("SELECT DATE(created_at) as date, COUNT(1) as count FROM t_exam_record WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY DATE(created_at) ORDER BY date")
        List<Map<String, Object>> countRecentActivity();

        @Select("SELECT " +
                        "s.college, " +
                        "COUNT(DISTINCT s.id) as studentCount, " +
                        "COUNT(DISTINCT er.id) as examCount, " +
                        "SUM(CASE WHEN er.abnormal_flag = 1 THEN 1 ELSE 0 END) as abnormalCount, " +
                        "ROUND(COUNT(DISTINCT er.student_id) * 100.0 / COUNT(DISTINCT s.id), 0) as rate " +
                        "FROM t_student s " +
                        "LEFT JOIN t_exam_record er ON s.id = er.student_id AND er.deleted = 0 " +
                        "AND er.batch_id = (SELECT id FROM t_exam_batch WHERE deleted = 0 AND status IN (1,2) ORDER BY start_date DESC LIMIT 1) "
                        +
                        "WHERE s.deleted = 0 " +
                        "GROUP BY s.college " +
                        "ORDER BY studentCount DESC " +
                        "LIMIT 10")
        List<Map<String, Object>> getCollegeStatistics();

        // Gender statistics
        @Select("SELECT CASE s.gender WHEN 1 THEN '男' WHEN 2 THEN '女' ELSE '未知' END as gender, " +
                        "COUNT(1) as count, " +
                        "SUM(CASE WHEN er.abnormal_flag = 1 THEN 1 ELSE 0 END) as abnormalCount " +
                        "FROM t_student s " +
                        "LEFT JOIN t_exam_record er ON s.id = er.student_id AND er.deleted = 0 " +
                        "WHERE s.deleted = 0 " +
                        "GROUP BY s.gender")
        List<Map<String, Object>> getGenderStatistics();

        // Grade statistics
        @Select("SELECT s.grade, COUNT(1) as count, " +
                        "SUM(CASE WHEN er.abnormal_flag = 1 THEN 1 ELSE 0 END) as abnormalCount " +
                        "FROM t_student s " +
                        "LEFT JOIN t_exam_record er ON s.id = er.student_id AND er.deleted = 0 " +
                        "WHERE s.deleted = 0 AND s.grade IS NOT NULL AND s.grade != '' " +
                        "GROUP BY s.grade ORDER BY s.grade")
        List<Map<String, Object>> getGradeStatistics();

        // Abnormal trend by batch (Last 10 batches)
        @Select("SELECT b.batch_name as batchName, " +
                        "COUNT(er.id) as total, " +
                        "SUM(CASE WHEN er.abnormal_flag = 1 THEN 1 ELSE 0 END) as abnormalCount, " +
                        "ROUND(SUM(CASE WHEN er.abnormal_flag = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(er.id), 2) as rate "
                        +
                        "FROM t_exam_batch b " +
                        "JOIN t_exam_record er ON b.id = er.batch_id " +
                        "WHERE b.deleted = 0 AND er.deleted = 0 " +
                        "GROUP BY b.id, b.batch_name " +
                        "ORDER BY b.start_date DESC LIMIT 10")
        List<Map<String, Object>> getAbnormalTrendByBatch();

        // Data for Clustering (BMI, SBP, DBP)
        @Select("SELECT " +
                        "s.id as studentId, s.student_no as studentNo, s.name as studentName, " +
                        "(SELECT value_decimal FROM t_exam_metric WHERE record_id = r.id AND metric_key = 'bmi' LIMIT 1) as bmi, "
                        +
                        "(SELECT value_decimal FROM t_exam_metric WHERE record_id = r.id AND metric_key = 'sbp' LIMIT 1) as sbp, "
                        +
                        "(SELECT value_decimal FROM t_exam_metric WHERE record_id = r.id AND metric_key = 'dbp' LIMIT 1) as dbp "
                        +
                        "FROM t_exam_record r " +
                        "JOIN t_student s ON r.student_id = s.id " +
                        "WHERE r.deleted = 0 AND r.abnormal_flag IS NOT NULL " +
                        "HAVING bmi IS NOT NULL AND sbp IS NOT NULL AND dbp IS NOT NULL " +
                        "LIMIT 1000")
        List<Map<String, Object>> getDataForClustering();

        // Student specific history for trend prediction
        @Select("SELECT b.start_date as date, m.value_decimal as value " +
                        "FROM t_exam_record r " +
                        "JOIN t_exam_batch b ON r.batch_id = b.id " +
                        "JOIN t_exam_metric m ON m.record_id = r.id " +
                        "WHERE r.student_id = #{studentId} AND m.metric_key = #{metricKey} " +
                        "AND r.deleted = 0 AND b.deleted = 0 " +
                        "ORDER BY b.start_date ASC")
        List<Map<String, Object>> getStudentMetricHistory(
                        @Param("studentId") Long studentId,
                        @Param("metricKey") String metricKey);
}
