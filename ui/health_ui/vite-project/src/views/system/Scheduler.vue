<template>
  <div class="page-header">
    <div class="header-left">
      <h1>定时任务</h1>
      <p>管理系统自动化调度任务</p>
    </div>
  </div>

  <div class="scheduler-grid">
    <div v-for="job in jobs" :key="job.id" class="card job-card">
      <div class="job-header">
        <div class="job-info">
          <h3>{{ job.jobName }}</h3>
          <span class="cron">{{ job.cronExpression }}</span>
        </div>
        <div class="job-status">
          <span class="dot" :class="job.status === 0 ? 'online' : 'offline'"></span>
          {{ job.status === 0 ? '运行中' : '已暂停' }}
        </div>
      </div>
      
      <div class="job-details">
        <p><strong>Bean:</strong> {{ job.beanName }}</p>
        <p><strong>Method:</strong> {{ job.methodName }}</p>
        <p v-if="job.remark" class="remark">{{ job.remark }}</p>
      </div>

      <div class="job-actions">
        <button class="btn btn-outline btn-sm" @click="handleRun(job.id)">
          <Play :size="14" /> 立即执行
        </button>
        <button v-if="job.status === 0" class="btn btn-outline btn-sm" @click="handleStatus(job.id, 1)">
          <Pause :size="14" /> 暂停
        </button>
        <button v-else class="btn btn-primary btn-sm" @click="handleStatus(job.id, 0)">
          <Play :size="14" /> 恢复
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Play, Pause, RefreshCw } from 'lucide-vue-next';
import { getJobs, runJob, updateJobStatus } from '../../api/system';

const jobs = ref([]);

const fetchData = async () => {
  jobs.value = await getJobs();
};

const handleRun = async (id) => {
  await runJob(id);
  alert('执行指令已发送');
};

const handleStatus = async (id, status) => {
  await updateJobStatus(id, status);
  fetchData();
};

onMounted(fetchData);
</script>

<style scoped>
.page-header { margin-bottom: 24px; }
.scheduler-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 24px; }
.job-card { display: flex; flex-direction: column; gap: 16px; }

.job-header { display: flex; justify-content: space-between; align-items: flex-start; }
.job-info h3 { font-size: 16px; font-weight: 500; }
.cron { font-size: 12px; color: var(--primary-color); font-family: monospace; }

.job-status { font-size: 12px; display: flex; align-items: center; gap: 6px; }
.dot { width: 8px; height: 8px; border-radius: 50%; }
.dot.online { background: var(--success); box-shadow: 0 0 8px var(--success); }
.dot.offline { background: var(--text-secondary); }

.job-details { font-size: 13px; color: var(--text-secondary); }
.remark { margin-top: 8px; font-style: italic; }

.job-actions { display: flex; gap: 12px; margin-top: 8px; }
.btn-sm { flex: 1; height: 32px; gap: 6px; }
</style>
