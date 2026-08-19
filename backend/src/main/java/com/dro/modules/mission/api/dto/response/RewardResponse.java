package com.dro.modules.mission.api.dto.response;

import com.dro.modules.inventory.domain.ItemType;

/**
 * Contrato de dados do módulo de Missões.
 */
public record RewardResponse(
        ItemType item,
        int quantity
) {}
