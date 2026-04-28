package com.example.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.attendance.entity.Department;
import com.example.attendance.entity.User;
import com.example.attendance.mapper.DepartmentMapper;
import com.example.attendance.mapper.UserMapper;
import com.example.attendance.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Department> getDepartmentTree() {
        return baseMapper.selectDepartmentWithChildren(0L);
    }

    @Override
    public List<Department> getAllDepartmentsWithDetails() {
        return baseMapper.selectAllDepartmentsWithDetails();
    }

    @Override
    public Department getDepartmentByIdWithDetails(Long id) {
        return baseMapper.selectDepartmentByIdWithDetails(id);
    }

    @Override
    public boolean createDepartment(Department department) {
        return save(department);
    }

    @Override
    public boolean updateDepartment(Department department) {
        return updateById(department);
    }

    @Override
    public boolean deleteDepartment(Long id) {
        if (hasChildren(id)) {
            return false;
        }
        List<User> users = userMapper.selectUsersByDeptId(id);
        if (!users.isEmpty()) {
            return false;
        }
        return removeById(id);
    }

    @Override
    public boolean hasChildren(Long id) {
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id).eq("deleted", 0);
        return count(queryWrapper) > 0;
    }
}
