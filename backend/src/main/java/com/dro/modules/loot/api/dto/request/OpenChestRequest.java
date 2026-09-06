package com.dro.modules.loot.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados necessários para abrir um baú do inventário.
 *
 * @param chestCode código persistido da definição do baú
 * @param requestId chave única da tentativa lógica, reutilizada em retries
 * @param quantity quantidade de baús a abrir; quando omitida, assume um
 * @param ignoreMaxStackItems quando {@code true}, itens que excederiam o limite máximo de
 *                            estoque não cancelam a abertura: o excedente é descartado e o
 *                            restante da recompensa é entregue normalmente
 */
public record OpenChestRequest(
        @NotBlank
        @Size(max = 100)
        String chestCode,
        @NotBlank
        @Size(max = 120)
        String requestId,
        @Min(1)
        @Max(999)
        Integer quantity,
        Boolean ignoreMaxStackItems
) {
    public OpenChestRequest(String chestCode, String requestId) {
        this(chestCode, requestId, 1, false);
    }

    public OpenChestRequest(String chestCode, String requestId, Integer quantity) {
        this(chestCode, requestId, quantity, false);
    }

    public int requestedQuantity() {
        return quantity == null ? 1 : quantity;
    }

    public boolean shouldIgnoreMaxStackItems() {
        return Boolean.TRUE.equals(ignoreMaxStackItems);
    }
}
