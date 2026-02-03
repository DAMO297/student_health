package org.example.exam;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamRecordMapper {
    ExamRecordEntity selectById(@Param("id") Long id);

    List<ExamRecordEntity> selectPage(
            @Param("batchId") Long batchId,
            @Param("studentId") Long studentId,
            @Param("doctorId") Long doctorId,
            @Param("abnormalFlag") Integer abnormalFlag,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    long count(
            @Param("batchId") Long batchId,
            @Param("studentId") Long studentId,
            @Param("doctorId") Long doctorId,
            @Param("abnormalFlag") Integer abnormalFlag
    );

    int insert(ExamRecordEntity e);

    int update(ExamRecordEntity e);

    int softDelete(@Param("id") Long id);
}





