package com.example.attendance.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("leave_approval")
public class LeaveApproval extends BaseEntity {

    private Long leaveId;

    private Long approverId;

    private String approverName;

    private Integer approvalLevel;

    private Integer approvalStatus;

    private String approvalComment;

    private LocalDateTime approvalTime;

    @TableField(exist = false)
    private String approvalLevelName;

    @TableField(exist = false)
    private String approvalStatusName;
}
