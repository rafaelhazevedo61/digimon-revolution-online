package com.dro.modules.digimon.api.dto.response;

import com.dro.modules.digimon.domain.enums.Rarity;

/**
 * Contrato de dados do módulo de Digimon.
 */
public record RebirthPreviewResponse(
        boolean eligible,
        String reason,
        int currentRebirthCount,
        int newRebirthCount,
        int costBits,
        int costDataCore,
        int currentDataCore,
        int costDigitalData,
        int currentDigitalData,
        int currentCodeInfinite,
        int currentBits,
        int remainingBitsAfterRebirth,
        IvRangeResponse hpIvRange,
        IvRangeResponse attackIvRange,
        IvRangeResponse defenseIvRange,
        double statMultiplier,
        int equippedEquipmentCount,
        Rarity currentRarity,
        int rarityPreservationItemQuantity
) {
}
