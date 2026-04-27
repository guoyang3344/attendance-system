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
}
