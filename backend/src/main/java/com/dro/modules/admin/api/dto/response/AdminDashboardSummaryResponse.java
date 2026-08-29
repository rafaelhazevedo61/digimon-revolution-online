package com.dro.modules.admin.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record AdminDashboardSummaryResponse(
        Metrics players,
        Metrics digimons,
        Metrics inventory,
        Metrics equipment,
        Metrics content,
        List<Alert> alerts,
        SystemStatus system
) {
    public record Metrics(long total, long secondary, long warnings) {}

    public record Alert(String severity, String code, String title, String message, long count) {}

    public record SystemStatus(String status, LocalDateTime generatedAt) {}
}

