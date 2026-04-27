package com.example.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class Role extends BaseEntity {

    private String roleName;

    private String roleCode;

    private String description;
}
