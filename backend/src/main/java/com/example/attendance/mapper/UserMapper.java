package com.example.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.attendance.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    List<User> selectUserWithRolesByUsername(@Param("username") String username);

    User selectUserWithRolesById(@Param("id") Long id);

    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    List<User> selectAllUsersWithDetails();

    User selectUserByIdWithDetails(@Param("id") Long id);

    List<User> selectUsersByParentId(@Param("parentId") Long parentId);

    List<User> selectUsersByDeptId(@Param("deptId") Long deptId);

    User selectSupervisorById(@Param("id") Long id);

    List<User> selectSubordinatesById(@Param("id") Long id);

    List<User> selectUserTree();
}
