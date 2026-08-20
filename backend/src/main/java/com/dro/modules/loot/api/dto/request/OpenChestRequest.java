package com.dro.modules.loot.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados necessários para abrir um baú do inventário.
 *
 * @param chestCode código persistido da definição do baú
 * @param requestId chave única da tentativa lógica, reutilizada em retries
 */
public record OpenChestRequest(
        @NotBlank
        @Size(max = 100)
        String chestCode,
        @NotBlank
        @Size(max = 120)
        String requestId
) {
}
