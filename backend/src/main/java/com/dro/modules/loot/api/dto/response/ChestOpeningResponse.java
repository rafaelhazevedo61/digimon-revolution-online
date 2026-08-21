package com.dro.modules.loot.api.dto.response;

import com.dro.modules.loot.domain.LootRarity;

import java.util.List;

/**
 * Resultado público de uma abertura de baú.
 *
 * <p>O campo {@code rarity} permanece como a primeira raridade sorteada para
 * compatibilidade com consumidores antigos. Em aberturas mistas, a raridade
 * autoritativa está em cada item de {@code items}.</p>
 */
public record ChestOpeningResponse(
        String requestId,
        String chestCode,
        String chestName,
        LootRarity rarity,
        List<ChestOpeningItemResponse> items,
        boolean replayed,
        String message
) {
}
