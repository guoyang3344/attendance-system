package com.example.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.attendance.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    List<Department> selectDepartmentWithChildren(@Param("parentId") Long parentId);

    List<Department> selectAllDepartmentsWithDetails();

    Department selectDepartmentByIdWithDetails(@Param("id") Long id);
}
