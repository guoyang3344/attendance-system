package com.example.attendance.controller;

import com.example.attendance.common.Result;
import com.example.attendance.entity.Permission;
import com.example.attendance.entity.Role;
import com.example.attendance.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/list")
    public Result<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return Result.success(roles);
    }

    @GetMapping("/{id}")
    public Result<Role> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        if (role == null) {
            return Result.error("角色不存在");
        }
        return Result.success(role);
    }

    @GetMapping("/{id}/permissions")
    public Result<List<Permission>> getRolePermissions(@PathVariable Long id) {
        List<Permission> permissions = roleService.getPermissionsByRoleId(id);
        return Result.success(permissions);
    }

    @PostMapping
    public Result<String> createRole(@RequestBody Role role) {
        boolean success = roleService.createRole(role);
        if (success) {
            return Result.success("创建成功");
        }
        return Result.error("创建失败");
    }

    @PutMapping
    public Result<String> updateRole(@RequestBody Role role) {
        boolean success = roleService.updateRole(role);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @PutMapping("/{id}/permissions")
    public Result<String> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> params) {
        List<Long> permissionIds = params.get("permissionIds");
        boolean success = roleService.assignPermissions(id, permissionIds);
        if (success) {
            return Result.success("权限分配成功");
        }
        return Result.error("权限分配失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteRole(@PathVariable Long id) {
        if (roleService.hasUsers(id)) {
            return Result.error("该角色下还有用户，无法删除");
        }
        boolean success = roleService.deleteRole(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
