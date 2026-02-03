<template>
  <div class="batch-page">
    <div class="page-header">
      <div class="header-left">
        <h1>体检批次管理</h1>
        <p>创建和管理体检批次</p>
      </div>
      <div class="header-actions">
        <button class="btn btn-primary" @click="openModal()">
          <Plus :size="18" /> 新增批次
        </button>
      </div>
    </div>

    <div class="card table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>批次名称</th>
            <th>开始日期</th>
            <th>结束日期</th>
            <th>状态</th>
            <th>备注</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="batches.length === 0">
            <td colspan="6" class="empty-row">暂无体检批次，请点击"新增批次"创建</td>
          </tr>
          <tr v-for="batch in batches" :key="batch.id">
            <td><strong>{{ batch.batchName }}</strong></td>
            <td>{{ batch.startDate || '-' }}</td>
            <td>{{ batch.endDate || '-' }}</td>
            <td>
              <span class="badge" :class="getStatusClass(batch.status)">
                {{ getStatusText(batch.status) }}
              </span>
            </td>
            <td>{{ batch.remark || '-' }}</td>
            <td>
              <div class="action-btns">
                <button class="btn btn-outline btn-sm" @click="openModal(batch)">
                  <Edit3 :size="14" /> 编辑
                </button>
                <button class="btn btn-danger btn-sm" @click="handleDelete(batch.id)">
                  <Trash2 :size="14" /> 删除
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Add/Edit Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card card">
        <div class="modal-header">
          <h3>{{ isEditMode ? '编辑' : '新增' }}体检批次</h3>
          <button class="btn-close" @click="closeModal">
            <X :size="20" />
          </button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label>批次名称 <span class="required">*</span></label>
            <input 
              type="text" 
              v-model="form.batchName" 
              placeholder="例如：2025年春季入学体检"
            />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>开始日期 <span class="required">*</span></label>
              <input type="date" v-model="form.startDate" />
            </div>

            <div class="form-group">
              <label>结束日期 <span class="required">*</span></label>
              <input type="date" v-model="form.endDate" />
            </div>
          </div>

          <div class="form-group">
            <label>状态</label>
            <select v-model.number="form.status">
              <option value="1">未开始</option>
              <option value="2">进行中</option>
              <option value="3">已结束</option>
              <option value="4">已归档</option>
            </select>
          </div>

          <div class="form-group">
            <label>备注</label>
            <textarea v-model="form.remark" rows="3" placeholder="选填"></textarea>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-outline" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="handleSubmit">
            {{ isEditMode ? '保存' : '创建' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { Plus, Edit3, Trash2, X } from 'lucide-vue-next';
import { 
  listBatches, 
  createBatch, 
  updateBatch, 
  deleteBatch 
} from '../../api/exam';

const batches = ref([]);
const showModal = ref(false);
const isEditMode = ref(false);

const form = ref({
  id: null,
  batchName: '',
  startDate: '',
  endDate: '',
  status: 2,
  remark: ''
});

// 移除 isFormValid 计算属性，改为点击时验证
// const isFormValid = computed(() => { ... });

const getStatusText = (status) => {
  const map = { 1: '未开始', 2: '进行中', 3: '已结束', 4: '已归档' };
  return map[status] || '未知';
};

const getStatusClass = (status) => {
  const map = {
    1: 'badge-warning',
    2: 'badge-success',
    3: 'badge-info',
    4: 'badge-secondary'
  };
  return map[status] || '';
};

const fetchBatches = async () => {
  try {
    batches.value = await listBatches();
  } catch (e) {
    console.error('获取批次失败:', e);
    alert('获取批次列表失败');
  }
};

const openModal = (batch = null) => {
  if (batch) {
    isEditMode.value = true;
    form.value = {
      id: batch.id,
      batchName: batch.batchName,
      startDate: batch.startDate,
      endDate: batch.endDate,
      status: batch.status,
      remark: batch.remark || ''
    };
  } else {
    isEditMode.value = false;
    form.value = {
      id: null,
      batchName: '',
      startDate: '',
      endDate: '',
      status: 2,
      remark: ''
    };
  }
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
};

const handleSubmit = async () => {
  // 手动验证，给出明确提示
  if (!form.value.batchName) {
    alert('请输入批次名称');
    return;
  }
  if (!form.value.startDate || !form.value.endDate) {
    alert('请选择开始日期和结束日期');
    return;
  }

  try {
    if (isEditMode.value) {
      await updateBatch(form.value.id, form.value);
      alert('批次更新成功！');
    } else {
      await createBatch(form.value);
      alert('批次创建成功！');
    }
    closeModal();
    fetchBatches();
  } catch (e) {
    alert('操作失败：' + (e.message || '未知错误'));
  }
};

const handleDelete = async (id) => {
  console.log('handleDelete called with id:', id);
  if (!window.confirm('确定要删除这个体检批次吗？删除后将无法恢复。')) {
    return;
  }
  try {
    await deleteBatch(id);
    alert('批次删除成功！');
    fetchBatches();
  } catch (e) {
    console.error('Delete failed:', e);
    alert('删除失败：' + (e.message || '未知错误'));
  }
};

onMounted(fetchBatches);
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 24px;
}

.table-card {
  padding: 0;
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  background: #f8f9fa;
  padding: 12px 24px;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
  font-size: 13px;
}

.data-table td {
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-color);
  font-size: 14px;
}

.empty-row {
  text-align: center;
  color: var(--text-secondary);
  padding: 40px !important;
}

.badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.badge-success { background: #e6f4ea; color: #1e8e3e; }
.badge-warning { background: #fff7e6; color: #faad14; }
.badge-info { background: #e8f0fe; color: #1a73e8; }
.badge-secondary { background: #f1f3f4; color: #5f6368; }
.badge-danger { background: #fce8e6; color: #d93025; }

.action-btns {
  display: flex;
  gap: 8px;
}

.btn-sm {
  height: 32px;
  padding: 0 12px;
  font-size: 13px;
}

.btn-danger {
  background: #fce8e6;
  color: #d93025;
  border: 1px solid #fce8e6;
}

.btn-danger:hover {
  background: #f6c7c2;
}

/* Modal */
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
  width: 600px;
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

.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.required {
  color: #d93025;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
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

.form-group textarea {
  resize: vertical;
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
