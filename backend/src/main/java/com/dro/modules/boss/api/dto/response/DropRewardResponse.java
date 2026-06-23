package com.dro.modules.boss.api.dto.response;

public record DropRewardResponse(
        String type,
        String code,
        String name,
        int quantity
) {}
