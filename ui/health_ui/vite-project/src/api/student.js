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
    // Use window.open or a direct link for binary downloads
    const query = new URLSearchParams(params).toString();
    window.open(`/api/students/export?${query}`, '_blank');
};
