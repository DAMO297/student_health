package org.example.exam;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamMetricMapper {
    List<ExamMetricEntity> selectByRecordId(@Param("recordId") Long recordId);

    int insertBatch(@Param("list") List<ExamMetricEntity> list);

    int deleteByRecordId(@Param("recordId") Long recordId);
}





