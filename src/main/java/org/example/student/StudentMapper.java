package org.example.student;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudentMapper {
        StudentEntity selectById(@Param("id") Long id);

        StudentEntity selectByStudentNo(@Param("studentNo") String studentNo);

        List<StudentEntity> selectPage(
                        @Param("studentNo") String studentNo,
                        @Param("name") String name,
                        @Param("college") String college,
                        @Param("grade") String grade,
                        @Param("clazz") String clazz,
                        @Param("status") Integer status,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        List<StudentEntity> selectList(
                        @Param("studentNo") String studentNo,
                        @Param("name") String name,
                        @Param("college") String college,
                        @Param("grade") String grade,
                        @Param("clazz") String clazz,
                        @Param("status") Integer status);

        long count(
                        @Param("studentNo") String studentNo,
                        @Param("name") String name,
                        @Param("college") String college,
                        @Param("grade") String grade,
                        @Param("clazz") String clazz,
                        @Param("status") Integer status);

        int insert(StudentEntity e);

        int update(StudentEntity e);

        int softDelete(@Param("id") Long id);
}

