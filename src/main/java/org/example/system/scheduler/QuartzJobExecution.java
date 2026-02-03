package org.example.system.scheduler;

import org.example.common.utils.SpringContextUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Date;

public class QuartzJobExecution implements Job {
    private static final Logger log = LoggerFactory.getLogger(QuartzJobExecution.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SchedulerJobEntity job = (SchedulerJobEntity) context.getMergedJobDataMap().get("JOB_PROPERTIES");
        long start = System.currentTimeMillis();
        try {
            log.info("任务开始执行 - 名称:{}", job.getJobName());
            Object bean = SpringContextUtil.getBean(job.getBeanName());
            Method method = bean.getClass().getDeclaredMethod(job.getMethodName(), String.class);
            ReflectionUtils.makeAccessible(method);
            method.invoke(bean, job.getMethodParams());
            long cost = System.currentTimeMillis() - start;
            log.info("任务执行结束 - 名称:{}, 耗时:{}ms", job.getJobName(), cost);
        } catch (Exception e) {
            log.error("任务执行失败 - 名称:{}", job.getJobName(), e);
            // 这里可以记录失败日志到数据库
        }
    }
}
