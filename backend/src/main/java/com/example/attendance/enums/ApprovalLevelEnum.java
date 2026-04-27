package com.example.attendance.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApprovalLevelEnum {

    TEAM_LEADER(1, "组长"),
    MANAGER(2, "经理"),
    CHAIRMAN(3, "董事长");

    private final Integer code;
    private final String name;

    public static String getNameByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ApprovalLevelEnum level : ApprovalLevelEnum.values()) {
            if (level.getCode().equals(code)) {
                return level.getName();
            }
        }
        return "未知";
    }

    public static ApprovalLevelEnum getNextLevel(Integer currentCode) {
        if (currentCode == null) {
            return null;
        }
        ApprovalLevelEnum[] values = ApprovalLevelEnum.values();
        for (int i = 0; i < values.length - 1; i++) {
            if (values[i].getCode().equals(currentCode)) {
                return values[i + 1];
            }
        }
        return null;
    }

    public static boolean isLastLevel(Integer code) {
        return CHAIRMAN.getCode().equals(code);
    }
}
