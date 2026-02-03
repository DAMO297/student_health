package org.example.exam;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamBatchMapper {
    ExamBatchEntity selectById(@Param("id") Long id);

    List<ExamBatchEntity> selectAll(@Param("status") Integer status);

    int insert(ExamBatchEntity e);

    int update(ExamBatchEntity e);

    int softDelete(@Param("id") Long id);
}





