<template>
  <div class="advice-container">
    <div class="page-header">
      <h1>健康建议与预警</h1>
      <p>基于您的体检数据，为您提供专业的医师建议与健康指导</p>
    </div>

    <div v-if="loading" class="loading-state flex-center">
      <div class="skeleton-advice" v-for="i in 3" :key="i"></div>
    </div>

    <div v-else-if="reports.length === 0" class="card empty-state flex-center">
      <HeartPulse :size="48" color="var(--text-secondary)" />
      <p>暂无健康建议。请在体检完成后关注此页面。</p>
    </div>

    <div v-else class="advice-list">
      <div v-for="report in reports" :key="report.id" class="card advice-card" :class="'level-' + getWarningLevel(report)">
        <div class="advice-header">
          <div class="header-left">
            <span class="report-title">健康报告评估 - {{ formatDate(report.generatedAt) }}</span>
            <span class="warning-badge" :class="'badge-' + getWarningLevel(report)">
              {{ getWarningText(report) }}
            </span>
          </div>
          <button class="pdf-btn" @click="downloadPdf(report.id)">
            <FileDown :size="18" />
            PDF 报告
          </button>
        </div>

        <div class="advice-body">
          <div class="section">
            <h4><Activity :size="16" /> 体检概况</h4>
            <p>{{ report.summary || '暂无系统概况' }}</p>
          </div>
          <div class="section">
            <h4><Stethoscope :size="16" /> 医师建议</h4>
            <div class="advice-text">{{ report.doctorAdvice || '医师正在评估中，请耐心等待...' }}</div>
          </div>
        </div>

        <div class="advice-footer">
          <span>评估医师: {{ report.doctorName || '系统自动评估' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { HeartPulse, FileDown, Activity, Stethoscope } from 'lucide-vue-next';
import request from '../../api/request';

const loading = ref(true);
const reports = ref([]);

const fetchData = async () => {
  try {
    loading.value = true;
    const res = await request.get('/reports', { params: { pageSize: 20 } });
    reports.value = res.list;
  } catch (e) {
    console.error('Failed to fetch advice', e);
  } finally {
    loading.value = false;
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};

const getWarningLevel = (report) => {
  // Mock logic for warning level based on status or abnormal markers
  // Assume status 1=Normal, 2=Attention, 3=Critical
  if (report.status === 2) return 'warning';
  if (report.status === 3) return 'danger';
  return 'info';
};

const getWarningText = (report) => {
  const level = getWarningLevel(report);
  if (level === 'danger') return '高风险预警';
  if (level === 'warning') return '健康提醒';
  return '健康状况良好';
};

const downloadPdf = (id) => {
  window.open(`/api/reports/${id}/pdf`, '_blank');
};

onMounted(fetchData);
</script>

<style scoped>
.advice-container { display: flex; flex-direction: column; gap: 24px; }
.page-header h1 { font-size: 24px; margin-bottom: 8px; }
.page-header p { color: var(--text-secondary); }

.advice-list { display: flex; flex-direction: column; gap: 24px; }

.advice-card {
  padding: 0;
  overflow: hidden;
  border-left: 6px solid #ccc;
}

.level-info { border-left-color: #1a73e8; }
.level-warning { border-left-color: #f9ab00; }
.level-danger { border-left-color: #d93025; }

.advice-header {
  padding: 20px 24px;
  background: #f8f9fa;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left { display: flex; align-items: center; gap: 16px; }
.report-title { font-weight: 500; font-size: 16px; }

.warning-badge { padding: 4px 12px; border-radius: 4px; font-size: 12px; font-weight: 600; }
.badge-info { background: #e8f0fe; color: #1967d2; }
.badge-warning { background: #fef7e0; color: #ea8600; }
.badge-danger { background: #fce8e6; color: #c5221f; }

.pdf-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.2s;
}

.pdf-btn:hover { background: #f1f3f4; }

.advice-body { padding: 24px; display: flex; flex-direction: column; gap: 20px; }

.section h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: var(--text-primary);
  font-size: 15px;
}

.advice-text {
  line-height: 1.6;
  color: #3c4043;
  padding: 16px;
  background: #fdfdfe;
  border: 1px dashed var(--border-color);
  border-radius: 8px;
}

.advice-footer {
  padding: 12px 24px;
  background: #f8f9fa;
  font-size: 13px;
  color: var(--text-secondary);
  text-align: right;
}

.loading-state { height: 400px; flex-direction: column; gap: 16px; width: 100%; }
.skeleton-advice { width: 100%; height: 200px; background: #eee; border-radius: 8px; animation: pulse 1.5s infinite; margin-bottom: 16px; }

@keyframes pulse {
  0% { opacity: 0.6; }
  50% { opacity: 0.8; }
  100% { opacity: 0.6; }
}

.flex-center { display: flex; align-items: center; justify-content: center; flex-direction: column; }
</style>
