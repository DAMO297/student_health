import request from './request';

export const getUserProfile = () => {
    return request.get('/user/profile');
};

/**
 * 更新个人资料
 */
export const updateProfile = (data) => {
    return request.put('/user/profile', data);
};

/**
 * 修改密码
 */
export const changePassword = (data) => {
    return request.put('/user/change-password', data);
};
