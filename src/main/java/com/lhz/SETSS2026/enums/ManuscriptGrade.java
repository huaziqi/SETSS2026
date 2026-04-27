package com.LHZ.SETSS2026.enums;

public enum ManuscriptGrade {

    A_PLUS("A+", "优秀"),
    A("A", "良好"),
    A_MINUS("A-", "较好"),
    B_PLUS("B+", "中等偏上"),
    B("B", "中等"),
    B_MINUS("B-", "中等偏下"),
    C_PLUS("C+", "及格偏上"),
    C("C", "及格"),
    C_MINUS("C-", "及格偏下"),
    D("D", "不及格"),
    F("F", "失败");

    private final String code;
    private final String description;

    ManuscriptGrade(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
