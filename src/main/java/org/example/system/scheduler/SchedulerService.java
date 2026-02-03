package org.example.system.scheduler;

import org.example.common.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class SchedulerService {

    private final SchedulerUtil schedulerUtil;
    private final SchedulerJobMapper schedulerJobMapper;

    public SchedulerService(SchedulerUtil schedulerUtil, SchedulerJobMapper schedulerJobMapper) {
        this.schedulerUtil = schedulerUtil;
        this.schedulerJobMapper = schedulerJobMapper;
    }

    @PostConstruct
    public void init() {
        List<SchedulerJobEntity> list = schedulerJobMapper.selectList(null, null);
        for (SchedulerJobEntity job : list) {
            schedulerUtil.createScheduleJob(job);
        }
    }

    public List<SchedulerJobEntity> list(String jobName, Integer status) {
        return schedulerJobMapper.selectList(jobName, status);
    }

    @Transactional
    public SchedulerJobEntity create(SchedulerJobEntity job, String operator) {
        job.setStatus(0); // default enabled
        job.setCreatedBy(operator);
        job.setUpdatedBy(operator);
        schedulerJobMapper.insert(job);
        schedulerUtil.createScheduleJob(job);
        return job;
    }

    @Transactional
    public SchedulerJobEntity update(Long id, SchedulerJobEntity req, String operator) {
        SchedulerJobEntity exist = schedulerJobMapper.selectById(id);
        if (exist == null)
            throw BizException.notFound("任务不存在");

        exist.setJobName(req.getJobName());
        exist.setJobGroup(req.getJobGroup());
        exist.setBeanName(req.getBeanName());
        exist.setMethodName(req.getMethodName());
        exist.setMethodParams(req.getMethodParams());
        exist.setCronExpression(req.getCronExpression());
        exist.setConcurrent(req.getConcurrent());
        exist.setRemark(req.getRemark());
        exist.setUpdatedBy(operator);

        schedulerJobMapper.update(exist);
        schedulerUtil.updateScheduleJob(exist);
        return exist;
    }

    @Transactional
    public void delete(Long id) {
        SchedulerJobEntity exist = schedulerJobMapper.selectById(id);
        if (exist == null)
            return;
        schedulerUtil.deleteJob(exist);
        schedulerJobMapper.deleteById(id);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        SchedulerJobEntity exist = schedulerJobMapper.selectById(id);
        if (exist == null)
            throw BizException.notFound("任务不存在");

        if (status == 1) {
            schedulerUtil.pauseJob(exist);
        } else {
            schedulerUtil.resumeJob(exist);
        }
        schedulerJobMapper.updateStatus(id, status);
    }

    public void run(Long id) {
        SchedulerJobEntity exist = schedulerJobMapper.selectById(id);
        if (exist == null)
            throw BizException.notFound("任务不存在");
        schedulerUtil.run(exist);
    }
}
