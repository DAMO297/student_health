<template>
  <div class="audit-container">
    <div class="page-header">
      <h1>用户审核管理</h1>
      <p>审核待处理的医生注册申请</p>
    </div>

    <div v-if="loading" class="loading-state card">
      <div class="spinner"></div>
      <span>加载中...</span>
    </div>

    <div v-else-if="pendingUsers.length === 0" class="card empty-state">
      <UserCheck :size="48" color="var(--text-secondary)" />
      <p>暂无待审核用户</p>
    </div>

    <div v-else class="users-table card">
      <table>
        <thead>
          <tr>
            <th>用户名</th>
            <th>显示名称</th>
            <th>用户类型</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in pendingUsers" :key="user.id">
            <td>{{ user.username }}</td>
            <td>{{ user.displayName }}</td>
            <td>
              <span class="badge badge-info">
                {{ user.userType === 2 ? '医生' : '学生' }}
              </span>
            </td>
            <td>{{ formatDate(user.createdAt) }}</td>
            <td>
              <div class="action-buttons">
                <button @click="approveUser(user.id)" class="btn btn-success btn-sm">
                  <Check :size="16" /> 通过
                </button>
                <button @click="rejectUser(user.id)" class="btn btn-danger btn-sm">
                  <X :size="16" /> 拒绝
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { UserCheck, Check, X } from 'lucide-vue-next';
import request from '../../api/request';

const loading = ref(true);
const pendingUsers = ref([]);

const fetchPendingUsers = async () => {
  try {
    loading.value = true;
    pendingUsers.value = await request.get('/users/pending');
  } catch (e) {
    console.error('Failed to fetch pending users', e);
  } finally {
    loading.value = false;
  }
};

const approveUser = async (id) => {
  if (!confirm('确认通过该用户的注册申请吗？')) return;
  try {
    await request.post(`/users/${id}/approve`);
    alert('审核通过！');
    fetchPendingUsers();
  } catch (e) {
    alert('操作失败');
  }
};

const rejectUser = async (id) => {
  if (!confirm('确认拒绝该用户的注册申请吗？')) return;
  try {
    await request.post(`/users/${id}/reject`);
    alert('已拒绝');
    fetchPendingUsers();
  } catch (e) {
    alert('操作失败');
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleString('zh-CN');
};

onMounted(fetchPendingUsers);
</script>

<style scoped>
.audit-container { display: flex; flex-direction: column; gap: 24px; }
.page-header h1 { font-size: 24px; margin-bottom: 8px; }
.page-header p { color: var(--text-secondary); }

.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px;
  gap: 16px;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.users-table {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: #f8f9fa;
}

th {
  text-align: left;
  padding: 12px 16px;
  font-weight: 500;
  border-bottom: 1px solid var(--border-color);
}

td {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
}

.badge {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.badge-info { background: #e8f0fe; color: #1967d2; }

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.btn-success {
  background: #1e8e3e;
  color: white;
}

.btn-success:hover {
  background: #188038;
}

.btn-danger {
  background: #d93025;
  color: white;
}

.btn-danger:hover {
  background: #c5221f;
}
</style>
