package com.example.attendance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.attendance.entity.User;

import java.util.List;

public interface UserService extends IService<User> {

    User login(String username, String password);

    User getUserWithRolesByUsername(String username);

    User getUserWithRolesById(Long id);

    List<String> getPermissionsByUserId(Long userId);
}
