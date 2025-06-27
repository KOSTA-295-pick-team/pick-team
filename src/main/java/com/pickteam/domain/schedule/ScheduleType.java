package com.pickteam.domain.schedule;

public enum ScheduleType {
    MEETING("회의"),
    DEADLINE("마감일"),
    WORKSHOP("워크샵"),
    VACATION("휴가"),
    OTHER("기타");

    private final String description;

    ScheduleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}