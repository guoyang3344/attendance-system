package com.example.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.attendance.entity.Permission;
import com.example.attendance.entity.Role;
import com.example.attendance.entity.RolePermission;
import com.example.attendance.entity.UserRole;
import com.example.attendance.mapper.PermissionMapper;
import com.example.attendance.mapper.RoleMapper;
import com.example.attendance.mapper.RolePermissionMapper;
import com.example.attendance.mapper.UserRoleMapper;
import com.example.attendance.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public List<Role> getAllRoles() {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0).orderByAsc("id");
        return list(queryWrapper);
    }

    @Override
    public Role getRoleById(Long id) {
        return getById(id);
    }

    @Override
    public boolean createRole(Role role) {
        return save(role);
    }

    @Override
    public boolean updateRole(Role role) {
        return updateById(role);
    }

    @Override
    @Transactional
    public boolean deleteRole(Long id) {
        if (hasUsers(id)) {
            return false;
        }
        QueryWrapper<RolePermission> rpWrapper = new QueryWrapper<>();
        rpWrapper.eq("role_id", id);
        rolePermissionMapper.delete(rpWrapper);
        return removeById(id);
    }

    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        return permissionMapper.selectPermissionsByRoleId(roleId);
    }

    @Override
    @Transactional
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        QueryWrapper<RolePermission> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("role_id", roleId);
        rolePermissionMapper.delete(deleteWrapper);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermissionMapper.insert(rolePermission);
            }
        }
        return true;
    }

    @Override
    public boolean hasUsers(Long roleId) {
        QueryWrapper<UserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", roleId);
        return userRoleMapper.selectCount(queryWrapper) > 0;
    }
}
