package com.dro.modules.activitycalendar.infra;

public interface ActivityMonthlyAggregateProjection {
    String getYearMonth();
    Long getPoints();
    Long getActivePlayers();
    Long getEvents();
}
