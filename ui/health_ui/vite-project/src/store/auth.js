import { defineStore } from 'pinia';
import request from '../api/request';

// 自动清理之前可能存入的错误数据
if (localStorage.getItem('userInfo') === 'undefined') {
    localStorage.removeItem('userInfo');
}

export const useAuthStore = defineStore('auth', {
    state: () => {
        const storedUser = localStorage.getItem('userInfo');
        let userInfo = null;
        try {
            if (storedUser && storedUser !== 'undefined') {
                userInfo = JSON.parse(storedUser);
            }
        } catch (e) {
            console.error('Failed to parse userInfo from localStorage', e);
        }
        return {
            token: localStorage.getItem('token') || null,
            userInfo: userInfo
        };
    },
    getters: {
        isLoggedIn: (state) => !!state.token
    },
    actions: {
        async login(username, password) {
            try {
                const data = await request.post('/auth/login', { username, password });
                this.token = data.accessToken;
                this.userInfo = data.userInfo;
                localStorage.setItem('token', data.accessToken);
                localStorage.setItem('userInfo', JSON.stringify(data.userInfo));
                return true;
            } catch (error) {
                return false;
            }
        },
        async register(username, password, displayName, userType = 3) {
            try {
                await request.post('/auth/register', { username, password, displayName, userType });
                return true;
            } catch (error) {
                return false;
            }
        },
        logout() {
            this.token = null;
            this.userInfo = null;
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            window.location.href = '/login';
        }
    }
});
