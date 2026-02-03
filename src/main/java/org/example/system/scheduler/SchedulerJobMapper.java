package org.example.system.scheduler;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SchedulerJobMapper {
    SchedulerJobEntity selectById(Long id);

    List<SchedulerJobEntity> selectList(@Param("jobName") String jobName, @Param("status") Integer status);

    int insert(SchedulerJobEntity job);

    int update(SchedulerJobEntity job);

    int deleteById(Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
