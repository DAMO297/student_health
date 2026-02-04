import request from './request';

export const getStudentPage = (params) => {
    return request.get('/students', { params });
};

// Alias for medical exam page
export const getStudentList = (params) => {
    return request.get('/students', { params });
};

export const createStudent = (data) => {
    return request.post('/students', data);
};

export const updateStudent = (id, data) => {
    return request.put(`/students/${id}`, data);
};

export const deleteStudent = (id) => {
    return request.delete(`/students/${id}`);
};

export const importStudents = (formData) => {
    return request.post('/students/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
};

export const exportStudents = (params) => {
    return request.get('/students/export', {
        params,
        responseType: 'blob' // 接收二进制数据
    }).then(response => {
        // 创建blob URL并触发下载
        const blob = new Blob([response.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `student_export_${Date.now()}.xlsx`;
        document.body.appendChild(link);
        link.click();
        // 清理
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    });
};

// ==================== 账号管理 ====================

/**
 * 获取学生绑定的账号信息
 */
export const getStudentAccount = (studentId) => {
    return request.get(`/students/${studentId}/account`);
};

/**
 * 为学生创建账号
 */
export const createStudentAccount = (studentId, password) => {
    return request.post(`/students/${studentId}/account`, { password });
};

/**
 * 重置学生账号密码
 */
export const resetStudentPassword = (studentId, newPassword) => {
    return request.put(`/students/${studentId}/account/reset-password`, { newPassword });
};

