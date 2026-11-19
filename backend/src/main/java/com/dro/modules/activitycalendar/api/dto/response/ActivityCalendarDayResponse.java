package com.dro.modules.activitycalendar.api.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public record ActivityCalendarDayResponse(
        LocalDate date,
        int dayOfMonth,
        int points,
        boolean goalReached,
        boolean rewardClaimed,
        Instant goalReachedAt,
        Instant rewardClaimedAt
) {}
