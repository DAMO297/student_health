<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-circle">H</div>
        <h1>加入 StudentHealth</h1>
        <p>创建您的账号以开始使用</p>
      </div>
      
      <form @submit.prevent="handleRegister" class="login-form">
        <div class="form-group">
          <label for="username">用户名</label>
          <input 
            type="text" 
            id="username" 
            v-model="form.username" 
            placeholder="4-20位字符"
            required
            minlength="4"
            maxlength="20"
          >
        </div>

        <div class="form-group">
          <label for="displayName">显示名称</label>
          <input 
            type="text" 
            id="displayName" 
            v-model="form.displayName" 
            placeholder="您的真实姓名或昵称"
            required
          >
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <input 
            type="password" 
            id="password" 
            v-model="form.password" 
            placeholder="6-20位字符"
            required
            minlength="6"
            maxlength="20"
          >
        </div>

        <div class="form-group">
          <label for="confirmPassword">确认密码</label>
          <input 
            type="password" 
            id="confirmPassword" 
            v-model="form.confirmPassword" 
            placeholder="重复输入密码"
            required
          >
        </div>

        <div class="form-group">
          <label>注册身份</label>
          <div class="role-selector">
            <label class="role-option">
              <input type="radio" v-model="form.userType" :value="3" name="userType">
              <span>学生</span>
            </label>
            <label class="role-option">
              <input type="radio" v-model="form.userType" :value="2" name="userType">
              <span>医生</span>
            </label>
          </div>
        </div>
        
        <button type="submit" class="btn btn-primary login-btn" :disabled="loading">
          {{ loading ? '注册中...' : '立即注册' }}
        </button>

        <div class="form-footer">
          <span>已有账号？</span>
          <router-link to="/login">返回登录</router-link>
        </div>
      </form>
      
      <div class="login-footer">
        <p>© 2026 StudentHealth System</p>
      </div>
    </div>

    <!-- Success Modal -->
    <div v-if="showSuccessModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-card">
        <div class="modal-icon success">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
            <polyline points="22 4 12 14.01 9 11.01"></polyline>
          </svg>
        </div>
        <h2>注册成功！</h2>
        <p v-if="isDoctor" class="modal-message">
          您的医生账号已提交审核，请等待管理员批准后登录。
        </p>
        <p v-else class="modal-message">
          您的学生账号已创建成功，现在可以登录使用了。
        </p>
        <button @click="closeModal" class="btn btn-primary">前往登录</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../store/auth';

const router = useRouter();
const auth = useAuthStore();
const loading = ref(false);

const form = reactive({
  username: '',
  displayName: '',
  password: '',
  confirmPassword: '',
  userType: 3
});

const showSuccessModal = ref(false);
const isDoctor = ref(false);

const handleRegister = async () => {
  if (form.password !== form.confirmPassword) {
    alert('两次输入的密码不一致');
    return;
  }

  loading.value = true;
  isDoctor.value = form.userType === 2;
  const success = await auth.register(form.username, form.password, form.displayName, form.userType);
  loading.value = false;
  
  if (success) {
    showSuccessModal.value = true;
  } else {
    alert('注册失败，请检查网络连接或稍后再试');
  }
};

const closeModal = () => {
  showSuccessModal.value = false;
  router.push('/login');
};
</script>

<style scoped>
/* Reuse login styles for consistency */
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-color);
  background-image: 
    radial-gradient(at 0% 0%, rgba(66, 133, 244, 0.05) 0, transparent 50%),
    radial-gradient(at 100% 100%, rgba(52, 168, 83, 0.05) 0, transparent 50%);
}

.login-card {
  width: 100%;
  max-width: 440px;
  background: #fff;
  border-radius: 8px;
  box-shadow: var(--shadow-md);
  padding: 40px;
  animation: slideUp 0.6s ease;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-circle {
  width: 48px;
  height: 48px;
  background: var(--primary-color);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 24px;
  margin: 0 auto 16px;
}

.login-header h1 {
  font-size: 24px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.login-header p {
  color: var(--text-secondary);
  font-size: 14px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.form-group input {
  width: 100%;
  height: 44px;
  padding: 0 12px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 14px;
  transition: all var(--transition-fast);
}

.form-group input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(26, 115, 232, 0.1);
}

.login-btn {
  width: 100%;
  height: 44px;
  margin-top: 8px;
}

.form-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
}

.form-footer a {
  margin-left: 8px;
  font-weight: 500;
}

.login-footer {
  margin-top: 32px;
  text-align: center;
  font-size: 12px;
  color: var(--text-secondary);
}

.role-selector {
  display: flex;
  gap: 16px;
}

.role-option {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  border: 2px solid var(--border-color);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.role-option:has(input:checked) {
  border-color: var(--primary-color);
  background: rgba(26, 115, 232, 0.05);
}

.role-option input {
  margin-right: 8px;
  width: auto;
  height: auto;
}

.role-option span {
  font-weight: 500;
}

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
  z-index: 1000;
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-card {
  background: white;
  border-radius: 8px;
  padding: 32px;
  max-width: 400px;
  text-align: center;
  animation: slideUp 0.3s;
}

.modal-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-icon.success {
  background: #e6f4ea;
  color: #1e8e3e;
}

.modal-card h2 {
  font-size: 20px;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.modal-message {
  color: var(--text-secondary);
  margin-bottom: 24px;
  line-height: 1.5;
}

.modal-card .btn {
  min-width: 120px;
}
</style>
