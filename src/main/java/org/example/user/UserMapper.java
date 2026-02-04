package org.example.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    UserEntity selectById(@Param("id") Long id);

    UserEntity selectByUsername(@Param("username") String username);

    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    int updateLastLoginAt(@Param("userId") Long userId);

    int insert(@Param("user") UserEntity user);

    Long selectRoleIdByCode(@Param("code") String code);

    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    List<UserEntity> selectByStatus(@Param("status") Integer status);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 根据学生ID查询用户账号
     */
    UserEntity selectByStudentId(@Param("studentId") Long studentId);

    /**
     * 更新用户密码
     */
    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    /**
     * 更新显示名称
     */
    int updateDisplayName(@Param("id") Long id, @Param("displayName") String displayName);
}
