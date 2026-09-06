package com.dro.modules.loot.api.dto.response;

import com.dro.modules.loot.domain.LootRarity;

import java.util.List;

/**
 * Resultado público de uma abertura de baú.
 *
 * <p>O campo {@code rarity} permanece como a primeira raridade sorteada para
 * compatibilidade com consumidores antigos. Em aberturas mistas, a raridade
 * autoritativa está em cada item de {@code items}. O campo {@code quantity}
 * informa quantos baús foram processados pela chave idempotente.</p>
 *
 * <p>{@code itemsAtStackLimit} lista os nomes dos itens cujo excedente foi descartado
 * porque o jogador optou por ignorar o limite máximo de estoque ao abrir o baú. Fica
 * vazia quando nenhum item foi afetado.</p>
 */
public record ChestOpeningResponse(
        String requestId,
        String chestCode,
        String chestName,
        LootRarity rarity,
        List<ChestOpeningItemResponse> items,
        int quantity,
        boolean replayed,
        String message,
        List<String> itemsAtStackLimit
) {
}
