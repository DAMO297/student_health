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

export const exportPdf = async (id) => {
    try {
        const response = await request.get(`/reports/${id}/pdf`, {
            responseType: 'blob'
        });

        // 创建blob URL并触发下载
        const blob = new Blob([response], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `health_report_${id}.pdf`;
        document.body.appendChild(link);
        link.click();

        // 清理
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    } catch (e) {
        console.error('下载PDF失败:', e);
        alert('下载PDF失败：' + (e.response?.data?.message || e.message || '未知错误'));
    }
};

export const exportReportExcel = async (params) => {
    try {
        const response = await request.get('/reports/export', {
            params,
            responseType: 'blob'
        });

        // 创建blob URL并触发下载
        const blob = new Blob([response], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `health_reports_${new Date().getTime()}.xlsx`;
        document.body.appendChild(link);
        link.click();

        // 清理
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    } catch (e) {
        console.error('导出Excel失败:', e);
        alert('导出Excel失败：' + (e.response?.data?.message || e.message || '未知错误'));
    }
};


export const generateBatchReports = (batchId) => {
    return request.post(`/reports/batch/${batchId}/generate`);
};

export const archiveBatchReports = (batchId) => {
    return request.post(`/reports/batch/${batchId}/archive`);
};

