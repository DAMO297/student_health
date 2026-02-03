<template>
  <div class="layout-container">
    <aside class="sidebar" :class="{ 'collapsed': isCollapsed }">
      <div class="logo-container">
        <div class="logo-circle">H</div>
        <span v-if="!isCollapsed" class="logo-text">StudentHealth</span>
      </div>
      
      <nav class="nav-menu">
        <router-link v-for="item in menuItems" :key="item.path" :to="item.path" class="nav-item">
          <component :is="item.icon" :size="20" />
          <span v-if="!isCollapsed">{{ item.name }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer" @click="isCollapsed = !isCollapsed">
        <ChevronLeft v-if="!isCollapsed" :size="20" />
        <ChevronRight v-else :size="20" />
      </div>
    </aside>

    <main class="main-content">
      <header class="header">
        <div class="header-breadcrumb">
          <span>{{ currentRouteName }}</span>
        </div>
        <div class="header-actions">
          <div class="user-info">
            <span>{{ auth.userInfo?.username }}</span>
            <div class="avatar">
              <User :size="18" />
            </div>
          </div>
          <button @click="handleLogout" class="btn-logout" title="退出登录">
            <LogOut :size="18" />
          </button>
        </div>
      </header>
      
      <div class="content-body">
        <router-view :key="$route.fullPath" />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from '../store/auth';
import { 
  LayoutDashboard, 
  Users, 
  Activity, 
  FileText, 
  Settings, 
  ShieldCheck, 
  Clock, 
  Calendar,
  LogOut,
  ChevronLeft,
  ChevronRight,
  User,
  BookOpen,
  UserCircle,
  History,
  HeartPulse,
  UserCheck
} from 'lucide-vue-next';

const route = useRoute();
const auth = useAuthStore();
const isCollapsed = ref(false);

const currentRouteName = computed(() => route.name);

const menuItems = computed(() => {
  const type = auth.userInfo?.userType;
  const allItems = [
    { name: '仪表盘', path: '/', icon: LayoutDashboard, roles: [1, 2] },
    { name: '学生管理', path: '/students', icon: Users, roles: [1] }, // 仅管理员可见
    { name: '体检批次管理', path: '/batches', icon: Calendar, roles: [1] }, // 仅管理员可见
    { name: '体检管理', path: '/exams', icon: Activity, roles: [1, 2] },
    { name: '健康报告', path: '/reports', icon: FileText, roles: [1, 2] },
    { name: '个人信息管理', path: '/student/profile', icon: UserCircle, roles: [3] },
    { name: '体检记录查看', path: '/student/records', icon: History, roles: [3] },
    { name: '健康建议与预警', path: '/student/advice', icon: HeartPulse, roles: [3] },
    { name: '数据字典', path: '/system/dict', icon: BookOpen, roles: [1] },
    { name: '用户审核', path: '/system/user-audit', icon: UserCheck, roles: [1] },
    { name: '审计日志', path: '/system/audit', icon: ShieldCheck, roles: [1] },
    { name: '定时任务', path: '/system/scheduler', icon: Clock, roles: [1] },
  ];
  return allItems.filter(item => item.roles.includes(type));
});

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    auth.logout();
  }
};
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* Sidebar */
.sidebar {
  width: 256px;
  background: #fff;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-slow);
  flex-shrink: 0;
}

.sidebar.collapsed {
  width: 72px;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 12px;
}

.logo-circle {
  width: 32px;
  height: 32px;
  background: var(--primary-color);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 500;
  color: var(--text-primary);
  white-space: nowrap;
}

.nav-menu {
  flex: 1;
  padding: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  height: 48px;
  padding: 0 16px;
  gap: 16px;
  color: var(--text-secondary);
  border-radius: 24px;
  margin-bottom: 4px;
  white-space: nowrap;
  transition: all var(--transition-fast);
}

.nav-item:hover {
  background-color: #f1f3f4;
  color: var(--text-primary);
}

.nav-item.router-link-active {
  background-color: #e8f0fe;
  color: var(--primary-color);
}

.sidebar-footer {
  height: 48px;
  border-top: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
}

/* Main Content */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 64px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  z-index: 10;
}

.header-breadcrumb {
  font-size: 18px;
  font-weight: 400;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f1f3f4;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
}

.btn-logout {
  padding: 8px;
  border-radius: 50%;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.btn-logout:hover {
  background: #fbe9e7;
  color: var(--google-red);
}

.content-body {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background-color: #f8f9fa;
}

/* Transitions */
.page-enter-active,
.page-leave-active {
  transition: all 0.3s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
