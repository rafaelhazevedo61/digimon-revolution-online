package com.dro.modules.boss.api.dto.response;

public record BossDropResponse(
        String dropType,
        String itemCode,
        String templateName,
        String equipmentRarity,
        int chance,
        int minQuantity,
        int maxQuantity
) {}
