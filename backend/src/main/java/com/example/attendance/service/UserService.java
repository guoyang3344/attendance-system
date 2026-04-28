package com.example.attendance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.attendance.entity.User;

import java.util.List;

public interface UserService extends IService<User> {

    User login(String username, String password);

    User getUserWithRolesByUsername(String username);

    User getUserWithRolesById(Long id);

    List<String> getPermissionsByUserId(Long userId);

    List<User> getAllUsersWithDetails();

    User getUserByIdWithDetails(Long id);

    List<User> getUsersByParentId(Long parentId);

    List<User> getUsersByDeptId(Long deptId);

    User getSupervisorById(Long id);

    List<User> getSubordinatesById(Long id);

    List<User> getUserTree();

    boolean createUser(User user, List<Long> roleIds);

    boolean updateUser(User user, List<Long> roleIds);

    boolean deleteUser(Long id);

    boolean assignRoles(Long userId, List<Long> roleIds);

    boolean usernameExists(String username, Long excludeId);
}
