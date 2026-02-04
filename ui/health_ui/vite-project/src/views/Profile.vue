<template>
  <div class="profile-container">
    <div class="page-header">
      <h1>个人中心</h1>
      <p>管理您的账号信息和密码</p>
    </div>

    <!-- 账号信息卡片 - 学生用户不显示 -->
    <div v-if="profile?.userType !== 3" class="card info-card">
      <div class="card-header">
        <h3>账号信息</h3>
      </div>
      <div class="card-body">
        <div class="info-row">
          <span class="label">用户名：</span>
          <span class="value">{{ profile?.username }}</span>
        </div>
        <div class="info-row">
          <span class="label">显示名称：</span>
          <span class="value">{{ profile?.displayName }}</span>
        </div>
        <div class="info-row">
          <span class="label">用户类型：</span>
          <span class="value">{{ getUserType(profile?.userType) }}</span>
        </div>
        <div class="info-row" v-if="profile?.studentId">
          <span class="label">关联学生：</span>
          <span class="value">学生ID: {{ profile.studentId }}</span>
        </div>
        <div class="info-row">
          <span class="label">账号状态：</span>
          <span class="badge" :class="profile?.status === 1 ? 'badge-success' : 'badge-error'">
            {{ profile?.status === 1 ? '正常' : '禁用' }}
          </span>
        </div>
        <div class="info-row">
          <span class="label">最后登录：</span>
          <span class="value">{{ formatDate(profile?.lastLoginAt) }}</span>
        </div>
      </div>
    </div>

    <!-- 编辑个人资料卡片 -->
    <div class="card edit-card">
      <div class="card-header">
        <h3>编辑资料</h3>
      </div>
      <div class="card-body">
        <div class="form-group">
          <label>显示名称</label>
          <input type="text" v-model="editForm.displayName" placeholder="请输入显示名称" />
        </div>
        <button class="btn btn-primary" @click="handleUpdateProfile">保存修改</button>
      </div>
    </div>

    <!-- 修改密码卡片 -->
    <div class="card password-card">
      <div class="card-header">
        <h3>修改密码</h3>
      </div>
      <div class="card-body">
        <div class="form-group">
          <label>旧密码 <span class="required">*</span></label>
          <input type="password" v-model="passwordForm.oldPassword" placeholder="请输入旧密码" />
        </div>
        <div class="form-group">
          <label>新密码 <span class="required">*</span></label>
          <input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码（至少6位）" />
        </div>
        <div class="form-group">
          <label>确认密码 <span class="required">*</span></label>
          <input type="password" v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
        </div>
        <button class="btn btn-primary" @click="handleChangePassword" :disabled="!isPasswordFormValid">修改密码</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { getUserProfile, updateProfile, changePassword } from '../api/user';

const profile = ref(null);
const editForm = ref({
  displayName: ''
});
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const fetchProfile = async () => {
  try {
    profile.value = await getUserProfile();
    editForm.value.displayName = profile.value.displayName;
  } catch (e) {
    console.error('获取个人信息失败:', e);
    alert('获取个人信息失败');
  }
};

const handleUpdateProfile = async () => {
  if (!editForm.value.displayName) {
    alert('显示名称不能为空');
    return;
  }

  try {
    await updateProfile(editForm.value);
    alert('个人资料更新成功！');
    await fetchProfile();
  } catch (e) {
    console.error('更新资料失败:', e);
    alert('更新资料失败：' + (e.response?.data?.message || e.message || '未知错误'));
  }
};

const handleChangePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    alert('两次密码输入不一致！');
    return;
  }

  if (passwordForm.value.newPassword.length < 6) {
    alert('密码长度至少6位！');
    return;
  }

  try {
    await changePassword(passwordForm.value);
    alert('密码修改成功！请重新登录。');
    // 清空表单
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    };
  } catch (e) {
    console.error('修改密码失败:', e);
    alert('修改密码失败：' + (e.response?.data?.message || e.message || '未知错误'));
  }
};

const getUserType = (type) => {
  const types = { 1: '管理员', 2: '医生', 3: '学生' };
  return types[type] || '未知';
};

const formatDate = (dateStr) => {
  if (!dateStr) return '从未登录';
  return new Date(dateStr).toLocaleString('zh-CN');
};

const isPasswordFormValid = computed(() => {
  return passwordForm.value.oldPassword && 
         passwordForm.value.newPassword && 
         passwordForm.value.confirmPassword &&
         passwordForm.value.newPassword.length >= 6;
});

onMounted(fetchProfile);
</script>

<style scoped>
.profile-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header h1 {
  font-size: 24px;
  margin-bottom: 8px;
}

.page-header p {
  color: var(--text-secondary);
}

.card {
  padding: 24px;
}

.card-header {
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}

.card-header h3 {
  font-size: 18px;
  font-weight: 600;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  font-weight: 500;
  color: hsl(var(--color-text) / 0.7);
  min-width: 120px;
}

.info-row .value {
  color: hsl(var(--color-text));
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  color: hsl(var(--color-text) / 0.8);
}

.form-group input {
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 14px;
}

.form-group input:focus {
  outline: none;
  border-color: var(--primary-color);
}

.required {
  color: #d93025;
}

.badge {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.badge-success {
  background: #e6f4ea;
  color: #1e8e3e;
}

.badge-error {
  background: #fce8e6;
  color: #d93025;
}

.btn {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: var(--primary-color);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.9;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
