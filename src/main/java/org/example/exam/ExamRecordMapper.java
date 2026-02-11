package org.example.exam;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.exam.dto.ExamRecordVO;

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
                        @Param("limit") int limit);

        long count(
                        @Param("batchId") Long batchId,
                        @Param("studentId") Long studentId,
                        @Param("doctorId") Long doctorId,
                        @Param("abnormalFlag") Integer abnormalFlag);

        /**
         * 分页查询体检记录（包含学生、医生、体检指标信息）
         */
        List<ExamRecordVO> selectPageVO(
                        @Param("batchId") Long batchId,
                        @Param("studentId") Long studentId,
                        @Param("doctorId") Long doctorId,
                        @Param("abnormalFlag") Integer abnormalFlag,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        List<ExamRecordEntity> selectByBatchId(@Param("batchId") Long batchId);

        int insert(ExamRecordEntity e);

        int update(ExamRecordEntity e);

        int softDelete(@Param("id") Long id);
}
