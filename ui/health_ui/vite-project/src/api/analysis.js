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
