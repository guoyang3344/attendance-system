package com.example.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department")
public class Department extends BaseEntity {

    private String deptName;

    private String deptCode;

    private Long parentId;

    private Integer sort;

    private String leaderId;

    private String description;

    @TableField(exist = false)
    private List<Department> children;

    @TableField(exist = false)
    private String parentName;

    @TableField(exist = false)
    private String leaderName;
}
