package com.example.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance_record")
public class AttendanceRecord extends BaseEntity {

    private Long userId;

    private LocalDateTime punchInTime;

    private LocalDateTime punchOutTime;

    private LocalDate punchDate;

    private Integer status;

    @TableField(exist = false)
    private String realName;

    @TableField(exist = false)
    private String statusName;
}
