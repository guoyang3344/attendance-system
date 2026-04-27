package com.example.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("leave_application")
public class LeaveApplication extends BaseEntity {

    private Long userId;

    private Long leaveTypeId;

    private String leaveTypeName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal leaveDays;

    private String reason;

    private Integer status;

    private Integer currentApprovalLevel;

    private Long nextApproverId;

    private Long rejectedBy;

    private String rejectedComment;

    private LocalDateTime rejectedTime;

    private LocalDateTime completedTime;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String statusName;

    @TableField(exist = false)
    private String currentApprovalLevelName;

    @TableField(exist = false)
    private String nextApproverName;

    @TableField(exist = false)
    private String rejectedByName;

    @TableField(exist = false)
    private List<LeaveApproval> approvals;
}
