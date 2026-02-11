import request from './request';

export const getOverview = () => {
    return request.get('/analysis/overview');
};

export const getActivity = () => {
    return request.get('/analysis/activity');
};

export const getCollegeStats = () => {
    return request.get('/analysis/college-stats');
};

export const getGenderStats = () => {
    return request.get('/analysis/gender-stats');
};

export const getGradeStats = () => {
    return request.get('/analysis/grade-stats');
};

export const getTrendStats = () => {
    return request.get('/analysis/trend-stats');
};

export const getRiskClusters = () => {
    return request.get('/analysis/risk-clusters');
};

export const getStudentTrend = (studentId, metricKey) => {
    return request.get('/analysis/student-trend', {
        params: { studentId, metricKey }
    });
};


