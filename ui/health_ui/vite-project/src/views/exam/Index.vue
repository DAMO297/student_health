<template>
  <div class="exam-page">
    <div class="page-header">
      <div class="header-left">
        <h1>体检管理</h1>
        <p>管理体检批次与详细检查记录</p>
      </div>
      <div class="header-actions">
        <button class="btn btn-primary" @click="openRecordModal">
          <Plus :size="18" /> 新增记录
        </button>
      </div>
    </div>

    <div class="exam-layout">
      <aside class="batch-sidebar card">
        <div class="card-header">
          <h3>体检批次</h3>
        </div>
        <div class="batch-list">
          <div 
            v-for="batch in batches" 
            :key="batch.id" 
            class="batch-item"
            :class="{ active: selectedBatchId === batch.id }"
            @click="selectBatch(batch.id)"
          >
            <Calendar :size="16" />
            <span>{{ batch.batchName }}</span>
          </div>
        </div>
      </aside>

      <div class="record-content card">
        <table class="data-table">
          <thead>
            <tr>
              <th>学生学号</th>
              <th>学生姓名</th>
              <th>医生</th>
              <th>异常标志</th>
              <th>体检信息</th>
              <th>检查时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="records.length === 0">
              <td colspan="7" class="empty-row">
                暂无体检记录，请点击"新增记录"添加
              </td>
            </tr>
            <tr v-for="record in records" :key="record.id">
              <td>{{ record.studentNo || '-' }}</td>
              <td>{{ record.studentName || '-' }}</td>
              <td>{{ record.doctorName || '-' }}</td>
              <td>
                <span class="badge" :class="record.abnormalFlag === 0 ? 'badge-success' : 'badge-error'">
                  {{ record.abnormalFlag === 0 ? '正常' : '异常' }}
                </span>
              </td>
              <td>
                身高: {{ record.height || '-' }}cm | 
                体重: {{ record.weight || '-' }}kg | 
                BMI: {{ record.bmi || '-' }}
              </td>
              <td>{{ formatDate(record.createdAt) }}</td>
              <td>
                <div class="action-btns">
                  <button class="btn btn-outline btn-sm" @click="handleEditRecord(record)">
                    <Edit3 :size="14" /> 编辑
                  </button>
                  <button class="btn btn-primary btn-sm" @click="handleGenerateReport(record.id)">
                    <FileText :size="14" /> 生成报告
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Add/Edit Record Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card card">
        <div class="modal-header">
          <h3>{{ isEditMode ? '编辑' : '新增' }}体检记录</h3>
          <button class="btn-close" @click="closeModal">
            <X :size="20" />
          </button>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <!-- 基础信息 -->
            <div class="form-section">
              <h4>基础信息</h4>
              
              <div class="form-group">
                <label>体检批次 <span class="required">*</span></label>
                <select v-model="form.batchId" :disabled="isEditMode">
                  <option value="">请选择批次</option>
                  <option v-for="batch in batches" :key="batch.id" :value="batch.id">
                    {{ batch.batchName }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label>学生 <span class="required">*</span></label>
                <select v-model="form.studentId" :disabled="isEditMode" @change="onStudentChange">
                  <option value="">请选择学生</option>
                  <option v-for="student in students" :key="student.id" :value="student.id">
                    {{ student.studentNo }} - {{ student.name }} ({{ student.college }})
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label>体检时间 <span class="required">*</span></label>
                <input type="datetime-local" v-model="form.examTime" />
              </div>
            </div>

            <!-- 体格检查 -->
            <div class="form-section">
              <h4>体格检查</h4>

              <div class="form-row">
                <div class="form-group">
                  <label>身高 (cm) <span class="required">*</span></label>
                  <input type="number" v-model.number="form.height" placeholder="例：175" step="0.1" />
                </div>

                <div class="form-group">
                  <label>体重 (kg) <span class="required">*</span></label>
                  <input type="number" v-model.number="form.weight" placeholder="例：65.5" step="0.1" />
                </div>

                <div class="form-group">
                  <label>BMI</label>
                  <input type="text" :value="calculatedBMI" disabled class="readonly" />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>收缩压 (mmHg)</label>
                  <input type="number" v-model.number="form.sbp" placeholder="例：120" />
                </div>

                <div class="form-group">
                  <label>舒张压 (mmHg)</label>
                  <input type="number" v-model.number="form.dbp" placeholder="例：80" />
                </div>

                <div class="form-group">
                  <label>心率 (bpm)</label>
                  <input type="number" v-model.number="form.heartRate" placeholder="例：72" />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>左眼视力</label>
                  <input type="number" v-model.number="form.visionL" placeholder="例：5.0" step="0.1" />
                </div>

                <div class="form-group">
                  <label>右眼视力</label>
                  <input type="number" v-model.number="form.visionR" placeholder="例：5.0" step="0.1" />
                </div>
              </div>
            </div>

            <!-- 备注 -->
            <div class="form-section full-width">
              <div class="form-group">
                <label>备注</label>
                <textarea v-model="form.remark" rows="3" placeholder="其他需要记录的信息"></textarea>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-outline" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="handleSubmit" :disabled="!isFormValid">
            {{ isEditMode ? '保存' : '提交' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { Plus, Calendar, FileText, Edit3, X } from 'lucide-vue-next';
import { listBatches, getRecordPage, createRecord, updateRecord } from '../../api/exam';
import { generateReport } from '../../api/report';
import { getStudentList } from '../../api/student';

const batches = ref([]);
const records = ref([]);
const students = ref([]);
const selectedBatchId = ref(null);
const showModal = ref(false);
const isEditMode = ref(false);

const form = ref({
  batchId: '',
  studentId: '',
  examTime: '',
  height: null,
  weight: null,
  sbp: null,
  dbp: null,
  heartRate: null,
  visionL: null,
  visionR: null,
  remark: ''
});

const calculatedBMI = computed(() => {
  if (form.value.height && form.value.weight) {
    const heightInMeters = form.value.height / 100;
    const bmi = form.value.weight / (heightInMeters * heightInMeters);
    return bmi.toFixed(1);
  }
  return '-';
});

const isFormValid = computed(() => {
  return form.value.batchId && form.value.studentId && 
         form.value.examTime && form.value.height && form.value.weight;
});

const fetchBatches = async () => {
  try {
    console.log('🔍 Fetching batches from API...');
    const response = await listBatches();
    console.log('📦 API Response:', response);
    console.log('📦 Response type:', typeof response);
    console.log('📦 Is Array?', Array.isArray(response));
    
    batches.value = response;
    console.log('✅ Batches loaded:', batches.value);
    console.log('📊 Batches count:', batches.value?.length || 0);
    
    if (batches.value && batches.value.length) {
      console.log('🎯 First batch:', batches.value[0]);
      selectBatch(batches.value[0].id);
    } else {
      console.warn('⚠️ No batches available!');
    }
  } catch (e) {
    console.error('❌ 获取批次失败:', e);
    console.error('❌ Error details:', e.message, e.response);
  }
};

const fetchStudents = async () => {
  try {
    const res = await getStudentList({ page: 1, pageSize: 1000 });
    students.value = res.list || [];
  } catch (e) {
    console.error('获取学生列表失败:', e);
  }
};

const selectBatch = async (id) => {
  selectedBatchId.value = id;
  try {
    const res = await getRecordPage({ batchId: id, page: 1, pageSize: 100 });
    records.value = res.list || [];
  } catch (e) {
    console.error('获取记录失败:', e);
    records.value = [];
  }
};

const openRecordModal = () => {
  if (batches.value.length === 0) {
    alert('请先创建体检批次！');
    return;
  }
  if (students.value.length === 0) {
    alert('暂无学生数据！请先添加学生信息。');
    return;
  }
  
  isEditMode.value = false;
  resetForm();
  form.value.batchId = selectedBatchId.value || (batches.value[0]?.id || '');
  // 设置当前时间
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  const hour = String(now.getHours()).padStart(2, '0');
  const minute = String(now.getMinutes()).padStart(2, '0');
  form.value.examTime = `${year}-${month}-${day}T${hour}:${minute}`;
  
  showModal.value = true;
};

const handleEditRecord = (record) => {
  isEditMode.value = true;
  form.value = {
    id: record.id,
    batchId: record.batchId,
    studentId: record.studentId,
    examTime: record.examTime,
    height: record.height,
    weight: record.weight,
    sbp: record.sbp,
    dbp: record.dbp,
    heartRate: record.heartRate,
    visionL: record.visionL,
    visionR: record.visionR,
    remark: record.remark || ''
  };
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  resetForm();
};

const resetForm = () => {
  form.value = {
    batchId: '',
    studentId: '',
    examTime: '',
    height: null,
    weight: null,
    sbp: null,
    dbp: null,
    heartRate: null,
    visionL: null,
    visionR: null,
    remark: ''
  };
};

const onStudentChange = () => {
  // 可以在这里预填充学生的历史数据
};

const handleSubmit = async () => {
  try {
    // 转换前端表单格式为后端期望的格式
    const payload = {
      batchId: form.value.batchId,
      studentId: form.value.studentId,
      recordTime: form.value.examTime.replace('T', ' ') + ':00', // 转换为 yyyy-MM-dd HH:mm:ss
      remark: form.value.remark || '',
      metrics: []
    };

    // 将体检指标转换为 metrics 数组
    if (form.value.height) {
      payload.metrics.push({
        metricKey: 'height',
        metricName: '身高',
        valueDecimal: form.value.height,
        unit: 'cm'
      });
    }
    
    if (form.value.weight) {
      payload.metrics.push({
        metricKey: 'weight',
        metricName: '体重',
        valueDecimal: form.value.weight,
        unit: 'kg'
      });
    }
    
    // 计算并添加 BMI
    if (form.value.height && form.value.weight) {
      const heightInMeters = form.value.height / 100;
      const bmi = form.value.weight / (heightInMeters * heightInMeters);
      payload.metrics.push({
        metricKey: 'bmi',
        metricName: 'BMI',
        valueDecimal: parseFloat(bmi.toFixed(1)),
        unit: ''
      });
    }
    
    if (form.value.sbp) {
      payload.metrics.push({
        metricKey: 'sbp',
        metricName: '收缩压',
        valueDecimal: form.value.sbp,
        unit: 'mmHg'
      });
    }
    
    if (form.value.dbp) {
      payload.metrics.push({
        metricKey: 'dbp',
        metricName: '舒张压',
        valueDecimal: form.value.dbp,
        unit: 'mmHg'
      });
    }
    
    if (form.value.heartRate) {
      payload.metrics.push({
        metricKey: 'heart_rate',
        metricName: '心率',
        valueDecimal: form.value.heartRate,
        unit: 'bpm'
      });
    }
    
    if (form.value.visionL) {
      payload.metrics.push({
        metricKey: 'vision_l',
        metricName: '左眼视力',
        valueDecimal: form.value.visionL,
        unit: ''
      });
    }
    
    if (form.value.visionR) {
      payload.metrics.push({
        metricKey: 'vision_r',
        metricName: '右眼视力',
        valueDecimal: form.value.visionR,
        unit: ''
      });
    }

    console.log('💾 提交数据:', payload);

    if (isEditMode.value) {
      await updateRecord(form.value.id, payload);
      alert('体检记录更新成功！');
    } else {
      await createRecord(payload);
      alert('体检记录添加成功！');
    }
    closeModal();
    selectBatch(selectedBatchId.value); // 刷新列表
  } catch (e) {
    console.error('❌ 保存失败:', e);
    alert('操作失败：' + (e.response?.data?.message || e.message || '未知错误'));
  }
};

const handleGenerateReport = async (recordId) => {
  try {
    await generateReport(recordId);
    alert('报告生成成功！可前往健康报告模块查看。');
  } catch (e) {
    alert('生成报告失败：' + (e.message || '未知错误'));
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('zh-CN');
};

onMounted(() => {
  fetchBatches();
  fetchStudents();
});
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 24px;
}

.exam-layout {
  display: flex;
  gap: 24px;
  height: calc(100vh - 200px);
}

.batch-sidebar {
  width: 240px;
  display: flex;
  flex-direction: column;
  padding: 16px 0;
  background: #fff !important;
  border: 2px solid red !important; /* DEBUG: 如果看到红框说明侧边栏存在 */
  min-width: 240px;
}

.batch-sidebar .card-header {
  padding: 0 24px 16px;
  border-bottom: 1px solid var(--border-color);
}

.batch-sidebar .card-header h3 {
  font-size: 16px;
  font-weight: 500;
  margin: 0;
}

.batch-list {
  flex: 1;
  overflow-y: auto;
}

.batch-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--text-secondary);
}

.batch-item:hover { background: #f1f3f4; color: var(--text-primary); }
.batch-item.active { background: #e8f0fe; color: var(--primary-color); font-weight: 500; }

.record-content {
  flex: 1;
  padding: 0;
  overflow: auto;
}

.data-table { width: 100%; border-collapse: collapse; }
.data-table th { background: #f8f9fa; padding: 12px 16px; text-align: left; border-bottom: 1px solid var(--border-color); font-size: 13px; }
.data-table td { padding: 14px 16px; border-bottom: 1px solid var(--border-color); font-size: 14px; }

.empty-row {
  text-align: center;
  color: var(--text-secondary);
  padding: 40px !important;
}

.badge { padding: 4px 8px; border-radius: 12px; font-size: 12px; font-weight: 500; }
.badge-success { background: #e6f4ea; color: #1e8e3e; }
.badge-error { background: #fce8e6; color: #d93025; }

.action-btns {
  display: flex;
  gap: 8px;
}

.btn-sm { height: 32px; padding: 0 12px; font-size: 13px; }

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-card {
  width: 900px;
  max-width: 90vw;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 500;
}

.btn-close {
  padding: 4px;
  border-radius: 4px;
  color: var(--text-secondary);
  transition: all 0.2s;
}

.btn-close:hover {
  background: #f1f3f4;
  color: var(--text-primary);
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-section.full-width {
  grid-column: 1 / -1;
}

.form-section h4 {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--primary-color);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.required {
  color: #d93025;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.2s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(66, 133, 244, 0.1);
}

.form-group input:disabled,
.form-group input.readonly {
  background: #f8f9fa;
  color: var(--text-secondary);
  cursor: not-allowed;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid var(--border-color);
}
</style>
