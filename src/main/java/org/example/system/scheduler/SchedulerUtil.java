package org.example.system.scheduler;

import org.example.common.BizException;
import org.quartz.*;
import org.springframework.stereotype.Component;

@Component
public class SchedulerUtil {

    private final Scheduler scheduler;

    public SchedulerUtil(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void createScheduleJob(SchedulerJobEntity job) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(QuartzJobExecution.class)
                    .withIdentity(getJobKey(job))
                    .build();

            jobDetail.getJobDataMap().put("JOB_PROPERTIES", job);

            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression())
                    .withMisfireHandlingInstructionDoNothing();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(getTriggerKey(job))
                    .withSchedule(scheduleBuilder)
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

            if (job.getStatus() == 1) {
                scheduler.pauseJob(getJobKey(job));
            }
        } catch (SchedulerException e) {
            throw new BizException(500, "创建定时任务失败: " + e.getMessage());
        }
    }

    public void updateScheduleJob(SchedulerJobEntity job) {
        try {
            TriggerKey triggerKey = getTriggerKey(job);
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression())
                    .withMisfireHandlingInstructionDoNothing();

            CronTrigger trigger = getCronTrigger(job);

            // Rebuild trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            trigger.getJobDataMap().put("JOB_PROPERTIES", job);

            scheduler.rescheduleJob(triggerKey, trigger);

            if (job.getStatus() == 1) {
                scheduler.pauseJob(getJobKey(job));
            }
        } catch (SchedulerException e) {
            throw new BizException(500, "更新定时任务失败: " + e.getMessage());
        }
    }

    public void run(SchedulerJobEntity job) {
        try {
            JobDataMap dataMap = new JobDataMap();
            dataMap.put("JOB_PROPERTIES", job);
            scheduler.triggerJob(getJobKey(job), dataMap);
        } catch (SchedulerException e) {
            throw new BizException("执行任务失败");
        }
    }

    public void pauseJob(SchedulerJobEntity job) {
        try {
            scheduler.pauseJob(getJobKey(job));
        } catch (SchedulerException e) {
            throw new BizException("暂停任务失败");
        }
    }

    public void resumeJob(SchedulerJobEntity job) {
        try {
            scheduler.resumeJob(getJobKey(job));
        } catch (SchedulerException e) {
            throw new BizException("恢复任务失败");
        }
    }

    public void deleteJob(SchedulerJobEntity job) {
        try {
            scheduler.deleteJob(getJobKey(job));
        } catch (SchedulerException e) {
            throw new BizException("删除任务失败");
        }
    }

    private TriggerKey getTriggerKey(SchedulerJobEntity job) {
        return TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());
    }

    private JobKey getJobKey(SchedulerJobEntity job) {
        return JobKey.jobKey(job.getJobName(), job.getJobGroup());
    }

    private CronTrigger getCronTrigger(SchedulerJobEntity job) {
        try {
            return (CronTrigger) scheduler.getTrigger(getTriggerKey(job));
        } catch (SchedulerException e) {
            throw new BizException("获取Trigger异常");
        }
    }
}
