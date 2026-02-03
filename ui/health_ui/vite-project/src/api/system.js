import request from './request';

// Dictionaries
export const getDictList = (typeCode) => {
    return request.get('/dict', { params: { typeCode } });
};

export const saveDict = (data) => {
    return data.id ? request.put(`/dict/${data.id}`, data) : request.post('/dict', data);
};

export const deleteDict = (id) => {
    return request.delete(`/dict/${id}`);
};

// Scheduler
export const getJobs = () => {
    return request.get('/scheduler');
};

export const runJob = (id) => {
    return request.post(`/scheduler/${id}/run`);
};

export const updateJobStatus = (id, status) => {
    return request.put(`/scheduler/${id}/status`, null, { params: { status } });
};

// Audit Logs (Note: Backend currently only has Mapper/Aspect, assuming /api/audit endpoint for UI)
export const getAuditLogs = (params) => {
    return request.get('/audit', { params });
};
