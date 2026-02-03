import request from './request';

export const getUserProfile = () => {
    return request.get('/users/profile');
};
