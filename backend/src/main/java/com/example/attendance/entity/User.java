package com.example.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private String username;

    private String password;

    private String realName;

    private String email;

    private String phone;

    private Integer status;

    private Long deptId;

    private Long parentId;

    private String position;

    @TableField(exist = false)
    private List<Role> roles;

    @TableField(exist = false)
    private List<String> permissions;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private String parentName;

    @TableField(exist = false)
    private List<User> children;
}
