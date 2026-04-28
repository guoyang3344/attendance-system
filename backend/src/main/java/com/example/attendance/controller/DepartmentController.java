package com.example.attendance.controller;

import com.example.attendance.common.Result;
import com.example.attendance.entity.Department;
import com.example.attendance.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping("/tree")
    public Result<List<Department>> getDepartmentTree() {
        List<Department> tree = departmentService.getDepartmentTree();
        return Result.success(tree);
    }

    @GetMapping("/list")
    public Result<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.getAllDepartmentsWithDetails();
        return Result.success(departments);
    }

    @GetMapping("/{id}")
    public Result<Department> getDepartmentById(@PathVariable Long id) {
        Department department = departmentService.getDepartmentByIdWithDetails(id);
        if (department == null) {
            return Result.error("部门不存在");
        }
        return Result.success(department);
    }

    @PostMapping
    public Result<String> createDepartment(@RequestBody Department department) {
        boolean success = departmentService.createDepartment(department);
        if (success) {
            return Result.success("创建成功");
        }
        return Result.error("创建失败");
    }

    @PutMapping
    public Result<String> updateDepartment(@RequestBody Department department) {
        boolean success = departmentService.updateDepartment(department);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteDepartment(@PathVariable Long id) {
        if (departmentService.hasChildren(id)) {
            return Result.error("该部门下有子部门，无法删除");
        }
        boolean success = departmentService.deleteDepartment(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败，可能该部门下还有员工");
    }
}
