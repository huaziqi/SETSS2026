package com.LHZ.SETSS2026.enums;

public enum ManuscriptStatus {

    AwaitingChecking("待审核"),
    UnderChecking("审核中"),
    AwaitingAssigning("待分配"),
    UnderAssigning("分配中"),
    AwaitingReviewing("待评审"),
    UnderReviewing("评审中"),
    Reviewed("已评审"),
    NonCompliant("判定违规");

    private final String description;

    ManuscriptStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
