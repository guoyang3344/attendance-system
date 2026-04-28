package com.example.attendance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.attendance.entity.Department;

import java.util.List;

public interface DepartmentService extends IService<Department> {

    List<Department> getDepartmentTree();

    List<Department> getAllDepartmentsWithDetails();

    Department getDepartmentByIdWithDetails(Long id);

    boolean createDepartment(Department department);

    boolean updateDepartment(Department department);

    boolean deleteDepartment(Long id);

    boolean hasChildren(Long id);
}
