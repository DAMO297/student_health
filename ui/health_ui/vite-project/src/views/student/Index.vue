<template>
  <div class="page-header">
    <div class="header-left">
      <h1>学生管理</h1>
      <p>管理在校学生的基础信息与联系方式</p>
    </div>
    <div class="header-actions">
      <button class="btn btn-outline" @click="showImportModal = true">
        <Upload :size="18" /> 导入
      </button>
      <button class="btn btn-outline" @click="handleExport">
        <Download :size="18" /> 导出
      </button>
      <button class="btn btn-primary" @click="openAddModal">
        <Plus :size="18" /> 新增学生
      </button>
    </div>
  </div>

  <div class="card search-card">
    <div class="search-form">
      <div class="form-group">
        <input v-model="filters.name" type="text" placeholder="姓名">
      </div>
      <div class="form-group">
        <input v-model="filters.studentNo" type="text" placeholder="学号">
      </div>
      <div class="form-group">
        <input v-model="filters.college" type="text" placeholder="学院">
      </div>
      <button class="btn btn-primary" @click="fetchData">查询</button>
      <button class="btn btn-outline" @click="resetFilters">重置</button>
    </div>
  </div>

  <div class="card table-card">
    <table class="data-table">
      <thead>
        <tr>
          <th>学号</th>
          <th>姓名</th>
          <th>性别</th>
          <th>学院</th>
          <th>班级</th>
          <th>手机号</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.studentNo }}</td>
          <td>{{ item.name }}</td>
          <td>{{ item.gender === 1 ? '男' : '女' }}</td>
          <td>{{ item.college }}</td>
          <td>{{ item.clazz }}</td>
          <td>{{ item.phone }}</td>
          <td>
            <span class="badge" :class="item.status === 1 ? 'badge-success' : 'badge-error'">
              {{ item.status === 1 ? '正常' : '禁用' }}
            </span>
          </td>
          <td>
            <div class="action-btns">
              <button class="icon-btn" @click="handleAccountManagement(item)" title="账号管理"><User :size="16" /></button>
              <button class="icon-btn" @click="openEditModal(item)" title="编辑"><Edit :size="16" /></button>
              <button class="icon-btn danger" @click="handleDelete(item)" title="删除"><Trash2 :size="16" /></button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
    
    <div class="pagination">
      <div class="pagination-left">
        <span>共 {{ total }} 条记录</span>
        <span class="divider">|</span>
        <span>第 {{ page }} / {{ totalPages }} 页</span>
      </div>
      <div class="pagination-right">
        <select v-model="pageSize" @change="handleSizeChange" class="page-size-select">
          <option :value="10">10条/页</option>
          <option :value="20">20条/页</option>
          <option :value="50">50条/页</option>
          <option :value="100">100条/页</option>
        </select>
        <div class="page-controls">
          <button :disabled="page === 1" @click="handlePageChange(page - 1)">上一页</button>
          <button :disabled="page >= totalPages" @click="handlePageChange(page + 1)">下一页</button>
        </div>
      </div>
    </div>
  </div>

  <!-- Add/Edit Modal -->
  <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
    <div class="modal-card card">
      <div class="modal-header">
        <h3>{{ editingId ? '编辑学生' : '新增学生' }}</h3>
        <button class="btn-close" @click="showModal = false"><X :size="20" /></button>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label>学号 <span class="required">*</span></label>
          <input v-model="form.studentNo" type="text" placeholder="请输入学号" :disabled="!!editingId">
        </div>
        <div class="form-group">
          <label>姓名 <span class="required">*</span></label>
          <input v-model="form.name" type="text" placeholder="请输入姓名">
        </div>
        <div class="form-group">
          <label>性别 <span class="required">*</span></label>
          <select v-model.number="form.gender">
            <option :value="1">男</option>
            <option :value="2">女</option>
          </select>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>学院</label>
            <input v-model="form.college" type="text" placeholder="所属学院">
          </div>
          <div class="form-group">
            <label>班级</label>
            <input v-model="form.clazz" type="text" placeholder="所属班级">
          </div>
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input v-model="form.phone" type="text" placeholder="联系电话">
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-outline" @click="showModal = false">取消</button>
        <button class="btn btn-primary" @click="handleSubmit">确定</button>
      </div>
    </div>
  </div>

  <!-- Import Modal -->
  <div v-if="showImportModal" class="modal-overlay" @click.self="showImportModal = false">
    <div class="modal-card card">
      <div class="modal-header">
        <h3>批量导入学生</h3>
        <button class="btn-close" @click="showImportModal = false"><X :size="20" /></button>
      </div>
      <div class="modal-body">
        <p class="import-hint">请上传 Excel 文件 (.xlsx, .xls)，包含：学号、姓名、性别、学院、班级等信息。</p>
        <div class="upload-area">
          <input type="file" ref="fileInput" accept=".xlsx, .xls" @change="handleFileChange">
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-outline" @click="showImportModal = false">取消</button>
        <button class="btn btn-primary" @click="handleImport" :disabled="!selectedFile">开始导入</button>
      </div>
    </div>
  </div>

  <!-- Account Management Modal -->
  <div v-if="showAccountModal" class="modal-overlay" @click.self="showAccountModal = false">
    <div class="modal-card card" style="max-width: 500px;">
      <div class="modal-header">
        <h3>账号管理 - {{ currentStudent?.name }}</h3>
        <button class="btn-close" @click="showAccountModal = false"><X :size="20" /></button>
      </div>

      <div class="modal-body">
        <div v-if="accountInfo" class="account-info">
          <div class="info-row">
            <span class="label">用户名：</span>
            <span class="value">{{ accountInfo.username }}</span>
          </div>
          <div class="info-row">
            <span class="label">状态：</span>
            <span class="badge" :class="accountInfo.status === 1 ? 'badge-success' : 'badge-error'">
              {{ accountInfo.status === 1 ? '正常' : '禁用' }}
            </span>
          </div>
          <div class="info-row">
            <span class="label">最后登录：</span>
            <span class="value">{{ accountInfo.lastLoginAt || '从未登录' }}</span>
          </div>

          <div class="form-group" style="margin-top: 20px;">
            <label>重置密码</label>
            <input 
              type="password" 
              v-model="accountPassword" 
              placeholder="输入新密码（6位以上）"
            />
          </div>

          <button 
            class="btn btn-primary" 
            style="width: 100%; margin-top: 10px;"
            @click="handleResetPassword"
            :disabled="!accountPassword || accountPassword.length < 6"
          >
            重置密码
          </button>
        </div>

        <div v-else class="no-account">
          <p>该学生尚未创建账号</p>
          <div class="form-group" style="margin-top: 20px;">
            <label>初始密码 <span class="required">*</span></label>
            <input 
              type="password" 
              v-model="accountPassword" 
              placeholder="设置初始密码（6位以上）"
            />
          </div>

          <button 
            class="btn btn-primary" 
            style="width: 100%; margin-top: 10px;"
            @click="handleCreateAccount"
            :disabled="!accountPassword || accountPassword.length < 6"
          >
            创建账号
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { Plus, Upload, Download, Edit, Trash2, Search, X, User } from 'lucide-vue-next';
import { 
  getStudentPage, 
  createStudent, 
  updateStudent, 
  deleteStudent, 
  importStudents, 
  exportStudents,
  getStudentAccount,
  createStudentAccount,
  resetStudentPassword
} from '../../api/student';

const list = ref([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const showModal = ref(false);
const editingId = ref(null);
const showImportModal = ref(false);
const selectedFile = ref(null);

// 账号管理相关状态
const showAccountModal = ref(false);
const currentStudent = ref(null);
const accountInfo = ref(null);
const accountPassword = ref('');

const filters = reactive({
  name: '',
  studentNo: '',
  college: ''
});

const form = reactive({
  id: null,
  studentNo: '',
  name: '',
  gender: 1,
  college: '',
  clazz: '',
  phone: '',
  status: 1
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getStudentPage({
      page: page.value,
      pageSize: pageSize.value,
      ...filters
    });
    list.value = res.list;
    total.value = res.total;
  } catch (e) {
    console.error(e);
    alert('获取数据失败');
  } finally {
    loading.value = false;
  }
};

const resetFilters = () => {
  filters.name = '';
  filters.studentNo = '';
  filters.college = '';
  fetchData();
};

const handleExport = () => {
  exportStudents(filters);
};

// ==================== 账号管理 ====================

const handleAccountManagement = async (student) => {
  currentStudent.value = student;
  accountPassword.value = '';
  
  try {
    const res = await getStudentAccount(student.id);
    accountInfo.value = res;
  } catch (e) {
    console.error('获取账号信息失败:', e);
    accountInfo.value = null;
  }
  
  showAccountModal.value = true;
};

const handleCreateAccount = async () => {
  if (!accountPassword.value || accountPassword.value.length < 6) {
    alert('密码至少需要6位！');
    return;
  }

  try {
    await createStudentAccount(currentStudent.value.id, accountPassword.value);
    alert(`账号创建成功！\n用户名：${currentStudent.value.studentNo}\n密码：${accountPassword.value}\n请告知学生妥善保管`);
    showAccountModal.value = false;
    // 刷新账号信息
    await handleAccountManagement(currentStudent.value);
  } catch (e) {
    console.error('创建账号失败:', e);
    alert('创建账号失败：' + (e.response?.data?.message || e.message || '未知错误'));
  }
};

const handleResetPassword = async () => {
  if (!accountPassword.value || accountPassword.value.length < 6) {
    alert('密码至少需要6位！');
    return;
  }

  if (!confirm('确定要重置该学生的登录密码吗？')) {
    return;
  }

  try {
    await resetStudentPassword(currentStudent.value.id, accountPassword.value);
    alert(`密码重置成功！\n新密码：${accountPassword.value}\n请告知学生妥善保管`);
    accountPassword.value = '';
  } catch (e) {
    console.error('重置密码失败:', e);
    alert('重置密码失败：' + (e.response?.data?.message || e.message || '未知错误'));
  }
};

const openAddModal = () => {
  editingId.value = null;
  form.id = null;
  form.studentNo = '';
  form.name = '';
  form.gender = 1;
  form.college = '';
  form.clazz = '';
  form.phone = '';
  form.status = 1;
  showModal.value = true;
};

const openEditModal = (item) => {
  editingId.value = item.id;
  form.id = item.id;
  form.studentNo = item.studentNo;
  form.name = item.name;
  form.gender = item.gender;
  form.college = item.college;
  form.clazz = item.clazz;
  form.phone = item.phone;
  form.status = item.status;
  showModal.value = true;
};

const handleSubmit = async () => {
  if (!form.studentNo || !form.name) {
    alert('学号和姓名不能为空');
    return;
  }
  
  try {
    if (editingId.value) {
      await updateStudent(form.id, form);
      alert('更新成功');
    } else {
      await createStudent(form);
      alert('创建成功');
    }
    showModal.value = false;
    fetchData();
  } catch (e) {
    alert('操作失败：' + (e.message || '未知错误'));
  }
};

const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value) || 1;
});

const handleSizeChange = () => {
  page.value = 1;
  fetchData();
};

const handlePageChange = (newPage) => {
  if (newPage < 1 || newPage > totalPages.value) return;
  page.value = newPage;
  fetchData();
};

const handleDelete = async (item) => {
  if (confirm(`确定要删除学生 ${item.name} 吗？`)) {
    try {
      await deleteStudent(item.id);
      alert('删除成功');
      fetchData();
    } catch (e) {
      alert('删除失败：' + (e.message || '未知错误'));
    }
  }
};

const handleFileChange = (e) => {
  selectedFile.value = e.target.files[0];
};

const handleImport = async () => {
  if (!selectedFile.value) return;
  
  const formData = new FormData();
  formData.append('file', selectedFile.value);
  
  try {
    await importStudents(formData);
    alert('导入成功');
    showImportModal.value = false;
    selectedFile.value = null;
    fetchData();
  } catch (e) {
    alert('导入失败：' + (e.message || '未知错误'));
  }
};

onMounted(fetchData);
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.header-left h1 {
  font-size: 28px;
  font-weight: 500;
  margin-bottom: 4px;
}

.header-left p {
  color: var(--text-secondary);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.search-card {
  margin-bottom: 24px;
  padding: 16px 24px;
}

.search-form {
  display: flex;
  gap: 16px;
  align-items: center;
}

.form-group input {
  height: 36px;
  padding: 0 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  width: 180px;
}

.table-card {
  padding: 0;
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.data-table th {
  background: #f8f9fa;
  padding: 12px 24px;
  font-weight: 500;
  color: var(--text-secondary);
  font-size: 14px;
  border-bottom: 1px solid var(--border-color);
}

.data-table td {
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-color);
  font-size: 14px;
}

.badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.badge-success { background: #e6f4ea; color: #1e8e3e; }
.badge-error { background: #fce8e6; color: #d93025; }

.action-btns {
  display: flex;
  gap: 8px;
}

.icon-btn {
  padding: 6px;
  border-radius: 4px;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.icon-btn:hover { background: #f1f3f4; color: var(--primary-color); }
.icon-btn.danger:hover { background: #fce8e6; color: var(--google-red); }

.pagination {
  padding: 16px 24px;
  display: flex;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: var(--text-secondary);
  border-top: 1px solid var(--border-color);
}

.pagination-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.pagination-right {
  display: flex;
  gap: 16px;
  align-items: center;
}

.divider {
  color: var(--border-color);
}

.page-size-select {
  height: 32px;
  padding: 0 8px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 13px;
  background-color: #fff;
  align-items: center;
  font-size: 14px;
  color: var(--text-secondary);
}

.page-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-controls button {
  padding: 4px 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background: #fff;
}

.page-controls button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-card {
  width: 500px;
  max-width: 90vw;
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
  margin: 0;
}

.btn-close {
  padding: 4px;
  border-radius: 4px;
  color: var(--text-secondary);
  cursor: pointer;
}

.btn-close:hover {
  background: #f1f3f4;
  color: var(--text-primary);
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 16px;
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
.form-group select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 14px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.import-hint {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.upload-area {
  padding: 20px;
  border: 2px dashed var(--border-color);
  border-radius: 8px;
  text-align: center;
}
</style>
