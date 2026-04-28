package com.example.attendance.controller;

import com.example.attendance.common.Result;
import com.example.attendance.entity.Department;
import com.example.attendance.entity.User;
import com.example.attendance.service.DepartmentService;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/my-info")
    public Result<Map<String, Object>> getMyOrganizationInfo(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        User userWithDetails = userService.getUserByIdWithDetails(currentUser.getId());
        User supervisor = userService.getSupervisorById(currentUser.getId());
        List<User> subordinates = userService.getSubordinatesById(currentUser.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("user", userWithDetails);
        result.put("supervisor", supervisor);
        result.put("subordinates", subordinates);

        return Result.success(result);
    }

    @GetMapping("/tree")
    public Result<List<User>> getOrganizationTree() {
        List<User> tree = userService.getUserTree();
        return Result.success(tree);
    }

    @GetMapping("/department-tree")
    public Result<List<Department>> getDepartmentTree() {
        List<Department> tree = departmentService.getDepartmentTree();
        return Result.success(tree);
    }

    @GetMapping("/user/{id}")
    public Result<Map<String, Object>> getUserOrganizationInfo(@PathVariable Long id) {
        User user = userService.getUserByIdWithDetails(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        User supervisor = userService.getSupervisorById(id);
        List<User> subordinates = userService.getSubordinatesById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("supervisor", supervisor);
        result.put("subordinates", subordinates);

        return Result.success(result);
    }

    @GetMapping("/level/{userId}")
    public Result<Map<String, Object>> getUserLevel(@PathVariable Long userId) {
        User user = userService.getUserByIdWithDetails(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        int level = 1;
        Long currentParentId = user.getParentId();
        while (currentParentId != null && currentParentId > 0) {
            User parent = userService.getById(currentParentId);
            if (parent == null || parent.getParentId() == null || parent.getParentId() == 0) {
                break;
            }
            level++;
            currentParentId = parent.getParentId();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("level", level);
        result.put("user", user);

        return Result.success(result);
    }
}
