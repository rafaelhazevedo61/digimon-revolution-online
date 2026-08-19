package com.dro.modules.boss.api.dto.response;

/**
 * Contrato de dados do módulo de Boss Mundial.
 */
public record DropRewardResponse(
        String type,
        String code,
        String name,
        int quantity,
        String rarity
) {}
