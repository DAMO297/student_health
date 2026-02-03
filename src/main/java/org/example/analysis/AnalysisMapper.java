package org.example.analysis;

import org.apache.ibatis.annotations.Mapper;
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

    // College statistics
    @Select("SELECT " +
            "s.college, " +
            "COUNT(DISTINCT s.id) as studentCount, " +
            "COUNT(DISTINCT er.id) as examCount, " +
            "SUM(CASE WHEN er.abnormal_flag = 1 THEN 1 ELSE 0 END) as abnormalCount, " +
            "ROUND(COUNT(DISTINCT er.id) * 100.0 / COUNT(DISTINCT s.id), 0) as rate " +
            "FROM t_student s " +
            "LEFT JOIN t_exam_record er ON s.id = er.student_id AND er.deleted = 0 " +
            "WHERE s.deleted = 0 " +
            "GROUP BY s.college " +
            "ORDER BY studentCount DESC " +
            "LIMIT 10")
    List<Map<String, Object>> getCollegeStatistics();
}
