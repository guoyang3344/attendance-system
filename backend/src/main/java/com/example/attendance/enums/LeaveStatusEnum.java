package com.example.attendance.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LeaveStatusEnum {

    PENDING(0, "待审批"),
    IN_PROGRESS(1, "审批中"),
    APPROVED(2, "已通过"),
    REJECTED(3, "已驳回");

    private final Integer code;
    private final String name;

    public static String getNameByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (LeaveStatusEnum status : LeaveStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status.getName();
            }
        }
        return "未知";
    }
}
