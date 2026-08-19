package com.dro.modules.boss.api.dto.response;

/**
 * Contrato de dados do módulo de Boss Mundial.
 */
public record BossDropResponse(
        String dropType,
        String itemCode,
        String templateName,
        String equipmentRarity,
        int chance,
        int minQuantity,
        int maxQuantity
) {}
