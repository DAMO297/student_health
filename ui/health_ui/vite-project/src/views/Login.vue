<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-circle">H</div>
        <h1>StudentHealth</h1>
        <p>学生健康管理系统</p>
      </div>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">用户名</label>
          <input 
            type="text" 
            id="username" 
            v-model="form.username" 
            placeholder="请输入用户名"
            required
          >
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <input 
            type="password" 
            id="password" 
            v-model="form.password" 
            placeholder="请输入密码"
            required
          >
        </div>
        
        <button type="submit" class="btn btn-primary login-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <div class="form-footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </form>
      
      <div class="login-footer">
        <p>© 2026 StudentHealth System</p>
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
  password: ''
});

const handleLogin = async () => {
  loading.value = true;
  const success = await auth.login(form.username, form.password);
  loading.value = false;
  
  if (success) {
    router.push('/');
  } else {
    alert('登录失败，请检查用户名和密码');
  }
};
</script>

<style scoped>
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
  max-width: 400px;
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
</style>
