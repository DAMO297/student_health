import request from './request';

export const getReportPage = (params) => {
    return request.get('/reports', { params });
};

export const generateReport = (recordId) => {
    return request.post('/reports/generate', { recordId });
};

export const updateAdvice = (id, data) => {
    return request.put(`/reports/${id}`, data);
};

export const exportPdf = (id) => {
    window.open(`/api/reports/${id}/pdf`, '_blank');
};

export const exportReportExcel = (params) => {
    const query = new URLSearchParams(params).toString();
    window.open(`/api/reports/export?${query}`, '_blank');
};
