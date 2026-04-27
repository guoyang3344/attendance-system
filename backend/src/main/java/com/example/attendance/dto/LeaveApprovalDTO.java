package com.example.attendance.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LeaveApprovalDTO {

    @NotNull(message = "请假申请ID不能为空")
    private Long leaveId;

    @NotNull(message = "审批状态不能为空")
    private Integer approvalStatus;

    private String approvalComment;
}
