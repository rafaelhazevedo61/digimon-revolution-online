package com.dro.modules.admin.api.dto.response;

import java.util.List;

public record AdminActivityMonthlyResponse(
        String from,
        String to,
        List<Month> months
) {
    public record Month(
            String yearMonth,
            long points,
            long activePlayers,
            long events
    ) {}
}
