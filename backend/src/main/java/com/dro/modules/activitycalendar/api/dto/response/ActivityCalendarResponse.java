package com.dro.modules.activitycalendar.api.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ActivityCalendarResponse(
        String yearMonth,
        int dailyGoal,
        String rewardChestCode,
        String monthlyCompletionChestCode,
        int daysInMonth,
        LocalDate currentDate,
        int currentDayPoints,
        boolean currentDayGoalReached,
        int claimedDays,
        boolean monthlyCompletionEligible,
        boolean monthlyRewardClaimed,
        Instant monthlyCompletionEligibleAt,
        Instant monthlyRewardClaimedAt,
        List<ActivityCalendarDayResponse> days
) {}
