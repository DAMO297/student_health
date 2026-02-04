import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../store/auth';

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: { public: true }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue'),
        meta: { public: true }
    },
    {
        path: '/',
        component: () => import('../layout/MainLayout.vue'),
        children: [
            {
                path: '',
                name: 'Dashboard',
                component: () => import('../views/dashboard/Index.vue')
            },
            {
                path: 'students',
                name: 'Students',
                component: () => import('../views/student/Index.vue')
            },
            {
                path: 'exams',
                name: 'Exams',
                component: () => import('../views/exam/Index.vue')
            },
            {
                path: 'batches',
                name: 'ExamBatches',
                component: () => import('../views/batch/Index.vue')
            },
            {
                path: 'reports',
                name: 'Reports',
                component: () => import('../views/report/Index.vue')
            },
            {
                path: 'student/profile',
                name: 'StudentProfile',
                component: () => import('../views/student/Profile.vue')
            },
            {
                path: 'student/records',
                name: 'StudentRecords',
                component: () => import('../views/student/Records.vue')
            },
            {
                path: 'student/advice',
                name: 'StudentAdvice',
                component: () => import('../views/student/Advice.vue')
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('../views/Profile.vue')
            },
            {
                path: 'system/audit',
                name: 'AuditLogs',
                component: () => import('../views/system/AuditLog.vue')
            },
            {
                path: 'system/scheduler',
                name: 'Scheduler',
                component: () => import('../views/system/Scheduler.vue')
            },
            {
                path: 'system/dict',
                name: 'Dictionaries',
                component: () => import('../views/system/Dictionary.vue')
            },
            {
                path: 'system/user-audit',
                name: 'UserAudit',
                component: () => import('../views/system/UserAudit.vue')
            }
        ]
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

router.beforeEach((to, from, next) => {
    const auth = useAuthStore();
    if (!to.meta.public && !auth.isLoggedIn) {
        next('/login');
    } else if (to.name === 'Login' && auth.isLoggedIn) {
        next('/');
    } else if (auth.isLoggedIn && to.path === '/') {
        // Role-based redirection for the root dashboard
        const type = auth.userInfo?.userType;
        if (type === 3) { // Student
            next('/student/records');
        } else {
            next();
        }
    } else {
        next();
    }
});

export default router;
