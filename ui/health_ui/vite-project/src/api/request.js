import axios from 'axios';

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
});

// Request Interceptor
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
      console.log('✅ Added Authorization header to request:', config.url);
    } else {
      console.warn('⚠️ No token found in localStorage for request:', config.url);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor
request.interceptors.response.use(
  (response) => {
    // 如果是blob类型的响应（例如文件下载），直接返回data
    if (response.config.responseType === 'blob') {
      return response.data;
    }

    const res = response.data;
    if (res.code === 200) {
      return res.data;
    }
    // Handle specific errors
    alert(res.message || 'Error');
    return Promise.reject(new Error(res.message || 'Error'));
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default request;
