package com.example.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("leave_type")
public class LeaveType extends BaseEntity {

    private String typeName;

    private String typeCode;

    private String description;

    private Integer sortOrder;

    private Integer status;
}
