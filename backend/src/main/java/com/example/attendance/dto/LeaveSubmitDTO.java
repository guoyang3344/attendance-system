package com.example.attendance.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LeaveSubmitDTO {

    @NotNull(message = "请假类型不能为空")
    private Long leaveTypeId;

    private String leaveTypeName;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @NotNull(message = "请假天数不能为空")
    private BigDecimal leaveDays;

    @NotBlank(message = "请假原因不能为空")
    private String reason;
}
