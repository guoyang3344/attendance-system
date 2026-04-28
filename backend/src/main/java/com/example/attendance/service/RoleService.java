package com.example.attendance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.attendance.entity.Permission;
import com.example.attendance.entity.Role;

import java.util.List;

public interface RoleService extends IService<Role> {

    List<Role> getAllRoles();

    Role getRoleById(Long id);

    boolean createRole(Role role);

    boolean updateRole(Role role);

    boolean deleteRole(Long id);

    List<Permission> getPermissionsByRoleId(Long roleId);

    boolean assignPermissions(Long roleId, List<Long> permissionIds);

    boolean hasUsers(Long roleId);
}
