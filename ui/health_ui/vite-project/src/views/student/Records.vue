<template>
  <div class="records-container">
    <div class="page-header">
      <h1>体检记录查看</h1>
      <p>回顾您在校期间的所有体检历史记录</p>
    </div>

    <div class="card table-card skeleton-container" v-if="loading">
      <div class="skeleton-row" v-for="i in 5" :key="i"></div>
    </div>

    <div v-else-if="records.length === 0" class="card empty-state flex-center">
      <Activity :size="48" color="var(--text-secondary)" />
      <p>暂无体检记录</p>
    </div>

    <div v-else class="records-list">
      <div v-for="record in records" :key="record.id" class="card record-card" @click="viewDetails(record)">
        <div class="record-main">
          <div class="record-info">
            <span class="batch-name">{{ record.batchName || '常规体检' }}</span>
            <span class="record-date">{{ formatDate(record.createdAt) }}</span>
          </div>
          <div class="record-status">
            <span class="badge" :class="record.abnormalFlag === 1 ? 'badge-error' : 'badge-success'">
              {{ record.abnormalFlag === 1 ? '指标异常' : '指标正常' }}
            </span>
          </div>
        </div>
        <div class="record-footer">
          <span>记录ID: {{ record.id }}</span>
          <ChevronRight :size="18" />
        </div>
      </div>
    </div>

    <!-- Details Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-card card">
        <div class="modal-header">
          <h3>体检详情 ({{ selectedRecord?.batchName }})</h3>
          <button class="close-btn" @click="showModal = false"><X :size="20" /></button>
        </div>
        <div class="modal-body">
          <table class="data-table">
            <thead>
              <tr>
                <th>检查项目</th>
                <th>结果</th>
                <th>单位</th>
                <th>参考范围</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="m in metrics" :key="m.id">
                <td>{{ m.metricName }}</td>
                <td :class="getValueClass(m)">
                  {{ m.valueText || m.valueDecimal }}
                  <span v-if="getAbnormalInfo(m).abnormal" class="abnormal-indicator" :class="`severity-${getAbnormalInfo(m).severity}`">
                    {{ getAbnormalInfo(m).msg }}
                  </span>
                </td>
                <td>{{ m.unit || '-' }}</td>
                <td>{{ getRefRange(m) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { Activity, ChevronRight, X } from 'lucide-vue-next';
import request from '../../api/request';
import { getReferenceRange, checkAbnormal } from '../../utils/examReferenceRanges';

const loading = ref(true);
const records = ref([]);
const metrics = ref([]);
const showModal = ref(false);
const selectedRecord = ref(null);
const studentGender = ref(null); // 学生性别

const fetchData = async () => {
  try {
    loading.value = true;
    const res = await request.get('/exam-records', { params: { pageSize: 50 } });
    records.value = res.list;
  } catch (e) {
    console.error('Failed to fetch records', e);
  } finally {
    loading.value = false;
  }
};

const viewDetails = async (record) => {
  selectedRecord.value = record;
  studentGender.value = record.studentGender || null; // 假设记录中有性别信息
  try {
    metrics.value = await request.get(`/exam-records/${record.id}/metrics`);
    showModal.value = true;
  } catch (e) {
    alert('获取详情失败');
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

/**
 * 获取参考范围文本
 */
const getRefRange = (metric) => {
  // 优先使用数据库中的参考范围
  if (metric.refLow !== null || metric.refHigh !== null) {
    if (metric.refLow !== null && metric.refHigh !== null) {
      return `${metric.refLow}-${metric.refHigh}`;
    }
    if (metric.refLow !== null) return `≥${metric.refLow}`;
    if (metric.refHigh !== null) return `≤${metric.refHigh}`;
  }
  
  // 使用智能算法获取标准参考范围
  return getReferenceRange(metric.metricKey, studentGender.value);
};

/**
 * 获取异常信息
 */
const getAbnormalInfo = (metric) => {
  // 优先检查数据库中的参考范围
  if (metric.refLow !== null || metric.refHigh !== null) {
    if (metric.valueDecimal < metric.refLow) {
      return { abnormal: true, severity: 'warning', msg: '偏低' };
    }
    if (metric.valueDecimal > metric.refHigh) {
      return { abnormal: true, severity: 'warning', msg: '偏高' };
    }
    return { abnormal: false, severity: 'normal', msg: '' };
  }
  
  // 使用智能算法检测
  return checkAbnormal(metric.metricKey, metric.valueDecimal, studentGender.value);
};

/**
 * 获取值的CSS类
 */
const getValueClass = (metric) => {
  const info = getAbnormalInfo(metric);
  if (!info.abnormal) return '';
  return info.severity === 'danger' ? 'text-danger' : 'text-warning';
};

onMounted(fetchData);
</script>

<style scoped>
.records-container { display: flex; flex-direction: column; gap: 24px; }
.page-header h1 { font-size: 24px; margin-bottom: 8px; }
.page-header p { color: var(--text-secondary); }

.records-list { display: flex; flex-direction: column; gap: 16px; }

.record-card {
  padding: 20px 24px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.record-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.record-main { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.record-info { display: flex; flex-direction: column; gap: 4px; }
.batch-name { font-size: 18px; font-weight: 500; }
.record-date { font-size: 14px; color: var(--text-secondary); }

.record-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
  font-size: 12px;
  color: var(--text-tertiary);
}

.badge { padding: 4px 12px; border-radius: 16px; font-size: 12px; font-weight: 500; }
.badge-success { background: #e6f4ea; color: #1e8e3e; }
.badge-error { background: #fce8e6; color: #d93025; }

.text-warning { color: #f9ab00; font-weight: 600; }
.text-danger { color: #d93025; font-weight: 600; }

.abnormal-indicator {
  display: inline-block;
  margin-left: 8px;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.severity-warning {
  background: #fef7e0;
  color: #f9ab00;
}

.severity-danger {
  background: #fce8e6;
  color: #d93025;
}

.flex-center { display: flex; align-items: center; justify-content: center; flex-direction: column; padding: 64px; }

.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-card { width: 800px; max-height: 80vh; overflow-y: auto; padding: 24px; }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.close-btn { background: none; border: none; cursor: pointer; color: var(--text-secondary); }

.data-table { width: 100%; border-collapse: collapse; }
.data-table th { text-align: left; padding: 12px; background: #f8f9fa; border-bottom: 1px solid var(--border-color); }
.data-table td { padding: 12px; border-bottom: 1px solid var(--border-color); }
</style>
