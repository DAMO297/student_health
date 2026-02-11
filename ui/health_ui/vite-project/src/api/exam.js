import request from './request';

export const listBatches = (status) => {
    return request.get('/exam-batches', { params: { status } });
};

export const createBatch = (data) => {
    return request.post('/exam-batches', data);
};

export const updateBatch = (id, data) => {
    return request.put(`/exam-batches/${id}`, data);
};

export const deleteBatch = (id) => {
    return request.delete(`/exam-batches/${id}`);
};

export const getRecordPage = (params) => {
    return request.get('/exam-records', { params });
};

export const createRecord = (data) => {
    return request.post('/exam-records', data);
};

export const updateRecord = (id, data) => {
    return request.put(`/exam-records/${id}`, data);
};

export const importRecords = (file, batchId) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('batchId', batchId);
    return request.post('/exam-records/import', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
};

