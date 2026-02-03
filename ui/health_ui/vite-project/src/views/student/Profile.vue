<template>
  <div class="profile-container">
    <div class="page-header">
      <h1>个人信息管理</h1>
      <p>查看并管理您的账户基础信息及学籍资料</p>
    </div>

    <div v-if="loading" class="loading-state card flex-center">
      <div class="spinner"></div>
      <span>正在加载个人资料...</span>
    </div>

    <div v-else class="profile-content">
      <!-- User Account Info -->
      <div class="card info-card">
        <div class="card-header">
          <User :size="20" />
          <h3>账号信息</h3>
        </div>
        <div class="info-grid">
          <div class="info-item">
            <label>用户名</label>
            <span>{{ profile.user?.username }}</span>
          </div>
          <div class="info-item">
            <label>显示名称</label>
            <span>{{ profile.user?.displayName }}</span>
          </div>
          <div class="info-item">
            <label>账号状态</label>
            <span class="badge" :class="profile.user?.status === 1 ? 'badge-success' : 'badge-error'">
              {{ profile.user?.status === 1 ? '正常' : '禁用' }}
            </span>
          </div>
          <div class="info-item">
            <label>上次登录</label>
            <span>{{ profile.user?.lastLoginAt || '暂无记录' }}</span>
          </div>
        </div>
      </div>

      <!-- Student Detail Info -->
      <div v-if="profile.student" class="card info-card">
        <div class="card-header">
          <GraduationCap :size="20" />
          <h3>学籍信息</h3>
        </div>
        <div class="info-grid">
          <div class="info-item">
            <label>学号</label>
            <span>{{ profile.student.studentNo }}</span>
          </div>
          <div class="info-item">
            <label>姓名</label>
            <span>{{ profile.student.name }}</span>
          </div>
          <div class="info-item">
            <label>学院</label>
            <span>{{ profile.student.college }}</span>
          </div>
          <div class="info-item">
            <label>年级</label>
            <span>{{ profile.student.grade }}</span>
          </div>
          <div class="info-item">
            <label>班级</label>
            <span>{{ profile.student.clazz }}</span>
          </div>
          <div class="info-item">
            <label>电子邮箱</label>
            <span>{{ profile.student.email || '未设置' }}</span>
          </div>
          <div class="info-item">
            <label>联系电话</label>
            <span>{{ profile.student.phone || '未设置' }}</span>
          </div>
          <div class="info-item">
            <label>性别</label>
            <span>{{ profile.student.gender === 1 ? '男' : '女' }}</span>
          </div>
        </div>
      </div>

      <div v-else class="card empty-card flex-center">
        <AlertCircle :size="48" color="var(--google-yellow)" />
        <p>您的账号尚未绑定具体的学籍信息，请联系管理员处理。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { User, GraduationCap, AlertCircle } from 'lucide-vue-next';
import { getUserProfile } from '../../api/user';

const loading = ref(true);
const profile = ref({
  user: null,
  student: null
});

const fetchData = async () => {
  try {
    loading.value = true;
    const res = await getUserProfile();
    profile.value = res;
  } catch (e) {
    console.error('Failed to fetch profile', e);
  } finally {
    loading.value = false;
  }
};

onMounted(fetchData);
</script>

<style scoped>
.profile-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header h1 {
  font-size: 24px;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.page-header p {
  color: var(--text-secondary);
}

.profile-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

.info-card {
  padding: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}

.card-header h3 {
  font-size: 18px;
  font-weight: 500;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item label {
  font-size: 12px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-item span {
  font-size: 15px;
  color: var(--text-primary);
}

.badge {
  display: inline-flex;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  width: fit-content;
}

.badge-success { background: #e6f4ea; color: #1e8e3e; }
.badge-error { background: #fce8e6; color: #d93025; }

.loading-state {
  height: 300px;
  flex-direction: column;
  gap: 16px;
}

.empty-card {
  height: 200px;
  flex-direction: column;
  gap: 12px;
  text-align: center;
  color: var(--text-secondary);
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

.flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
