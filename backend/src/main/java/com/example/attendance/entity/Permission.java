package com.example.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class Permission extends BaseEntity {

    private String permissionName;

    private String permissionCode;

    private Integer resourceType;

    private Long parentId;

    private String path;

    @TableField(exist = false)
    private List<Permission> children;
}
