package org.example.report;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.report.dto.ReportExcelModel;
import java.util.List;

@Mapper
public interface ReportMapper {
        ReportEntity selectById(@Param("id") Long id);

        ReportEntity selectByRecordId(@Param("recordId") Long recordId);

        List<ReportEntity> selectPage(
                        @Param("recordId") Long recordId,
                        @Param("studentId") Long studentId,
                        @Param("status") Integer status,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        List<ReportExcelModel> selectExportList(
                        @Param("recordId") Long recordId,
                        @Param("status") Integer status);

        long count(@Param("recordId") Long recordId, @Param("studentId") Long studentId,
                        @Param("status") Integer status);

        int insert(ReportEntity e);

        int updateAdvice(@Param("id") Long id, @Param("doctorAdvice") String doctorAdvice,
                        @Param("summary") String summary,
                        @Param("updatedBy") String updatedBy);

        int archiveByBatchId(@Param("batchId") Long batchId, @Param("operator") String operator);

}
