package com.example.attendance.controller;

import com.example.attendance.common.Result;
import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsersWithDetails();
        return Result.success(users);
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserByIdWithDetails(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    @GetMapping("/tree")
    public Result<List<User>> getUserTree() {
        List<User> tree = userService.getUserTree();
        return Result.success(tree);
    }

    @GetMapping("/{id}/supervisor")
    public Result<User> getSupervisor(@PathVariable Long id) {
        User supervisor = userService.getSupervisorById(id);
        return Result.success(supervisor);
    }

    @GetMapping("/{id}/subordinates")
    public Result<List<User>> getSubordinates(@PathVariable Long id) {
        List<User> subordinates = userService.getSubordinatesById(id);
        return Result.success(subordinates);
    }

    @GetMapping("/check-username")
    public Result<Map<String, Boolean>> checkUsername(
            @RequestParam String username,
            @RequestParam(required = false) Long excludeId) {
        boolean exists = userService.usernameExists(username, excludeId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("exists", exists);
        return Result.success(result);
    }

    @PostMapping
    public Result<String> createUser(@RequestBody Map<String, Object> params) {
        User user = new User();
        user.setUsername((String) params.get("username"));
        user.setPassword((String) params.get("password"));
        user.setRealName((String) params.get("realName"));
        user.setEmail((String) params.get("email"));
        user.setPhone((String) params.get("phone"));
        user.setStatus((Integer) params.get("status"));
        user.setDeptId(params.get("deptId") != null ? Long.valueOf(params.get("deptId").toString()) : null);
        user.setParentId(params.get("parentId") != null ? Long.valueOf(params.get("parentId").toString()) : null);
        user.setPosition((String) params.get("position"));

        @SuppressWarnings("unchecked")
        List<Long> roleIds = (List<Long>) params.get("roleIds");

        if (userService.usernameExists(user.getUsername(), null)) {
            return Result.error("用户名已存在");
        }

        boolean success = userService.createUser(user, roleIds);
        if (success) {
            return Result.success("创建成功");
        }
        return Result.error("创建失败");
    }

    @PutMapping
    public Result<String> updateUser(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") != null ? Long.valueOf(params.get("id").toString()) : null;
        if (id == null) {
            return Result.error("用户ID不能为空");
        }

        User user = new User();
        user.setId(id);
        user.setUsername((String) params.get("username"));
        user.setPassword((String) params.get("password"));
        user.setRealName((String) params.get("realName"));
        user.setEmail((String) params.get("email"));
        user.setPhone((String) params.get("phone"));
        user.setStatus((Integer) params.get("status"));
        user.setDeptId(params.get("deptId") != null ? Long.valueOf(params.get("deptId").toString()) : null);
        user.setParentId(params.get("parentId") != null ? Long.valueOf(params.get("parentId").toString()) : null);
        user.setPosition((String) params.get("position"));

        @SuppressWarnings("unchecked")
        List<Long> roleIds = (List<Long>) params.get("roleIds");

        if (userService.usernameExists(user.getUsername(), id)) {
            return Result.error("用户名已存在");
        }

        boolean success = userService.updateUser(user, roleIds);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    @PutMapping("/{id}/roles")
    public Result<String> assignRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> params) {
        List<Long> roleIds = params.get("roleIds");
        boolean success = userService.assignRoles(id, roleIds);
        if (success) {
            return Result.success("角色分配成功");
        }
        return Result.error("角色分配失败");
    }
}
