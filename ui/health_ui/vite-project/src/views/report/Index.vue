<template>
  <div class="page-header">
    <div class="header-left">
      <h1>健康报告</h1>
      <p>查看并管理体检生成的详细健康报告</p>
    </div>
    <div class="header-actions">
      <button class="btn btn-outline" @click="handleExportExcel">
        <Download :size="18" /> 导出报告清单
      </button>
    </div>
  </div>

  <div class="card table-card">
    <table class="data-table">
      <thead>
        <tr>
          <th>报告单号</th>
          <th>学号</th>
          <th>学生</th>
          <th>体检批次</th>
          <th>状态</th>
          <th>生成时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.reportNo || '#' + item.id }}</td>
          <td>{{ item.studentNo || '-' }}</td>
          <td>{{ item.studentName || '-' }}</td>
          <td>{{ item.batchName || '-' }}</td>
          <td>
            <span class="badge" :class="item.status === 2 ? 'badge-success' : 'badge-warning'">
              {{ item.status === 2 ? '已完成' : '待审核' }}
            </span>
          </td>
          <td>{{ item.createdAt }}</td>
          <td>
            <div class="action-btns">
              <button class="btn btn-outline btn-sm" @click="handleExportPdf(item.id)">
                <FileText :size="14" /> PDF
              </button>
              <button class="btn btn-primary btn-sm" @click="openEditModal(item)">
                <Edit3 :size="14" /> 建议
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <!-- Advice Modal -->
  <div v-if="showModal" class="modal-overlay">
    <div class="modal-card card">
      <h3>编辑医生建议</h3>
      <div class="modal-form">
        <div class="form-group">
          <label>摘要</label>
          <textarea v-model="form.summary" placeholder="简要健康状况总结"></textarea>
        </div>
        <div class="form-group">
          <label>医生建议</label>
          <textarea v-model="form.doctorAdvice" placeholder="详细的饮食、运动及后续检查建议"></textarea>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-outline" @click="showModal = false">取消</button>
        <button class="btn btn-primary" @click="handleSaveAdvice">保存</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { Download, FileText, Edit3 } from 'lucide-vue-next';
import { getReportPage, updateAdvice, exportPdf, exportReportExcel } from '../../api/report';

const list = ref([]);
const total = ref(0);
const showModal = ref(false);
const editingItem = ref(null);

const form = reactive({
  summary: '',
  doctorAdvice: ''
});

const fetchData = async () => {
  const res = await getReportPage({ page: 1, pageSize: 20 });
  list.value = res.list;
  total.value = res.total;
};

const openEditModal = (item) => {
  editingItem.value = item;
  form.summary = item.summary || '';
  form.doctorAdvice = item.doctorAdvice || '';
  showModal.value = true;
};

const handleSaveAdvice = async () => {
  await updateAdvice(editingItem.value.id, { ...form });
  showModal.value = false;
  fetchData();
};

const handleExportPdf = (id) => exportPdf(id);
const handleExportExcel = () => exportReportExcel({});

onMounted(fetchData);
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; margin-bottom: 24px; }
.table-card { padding: 0; overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { background: #f8f9fa; padding: 12px 24px; text-align: left; border-bottom: 1px solid var(--border-color); }
.data-table td { padding: 16px 24px; border-bottom: 1px solid var(--border-color); font-size: 14px; }

.badge { padding: 4px 8px; border-radius: 12px; font-size: 12px; }
.badge-success { background: #e6f4ea; color: #1e8e3e; }
.badge-warning { background: #fff7e6; color: #faad14; }

.btn-sm { padding: 0 12px; height: 32px; font-size: 13px; gap: 4px; }

.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal-card { width: 600px; padding: 24px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 500; }
.form-group textarea { width: 100%; height: 120px; padding: 12px; border: 1px solid var(--border-color); border-radius: 4px; resize: none; font-family: inherit; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }
</style>
