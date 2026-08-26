package com.dro.modules.admin.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Item específico do catálogo incluído em uma premiação de evento. */
public record AdminEventRewardItemRequest(
        @NotBlank(message = "Informe o código da definição do item.")
        @Size(max = 128, message = "O código da definição deve ter no máximo 128 caracteres.")
        String itemDefinitionCode,
        @Positive(message = "A quantidade do item deve ser maior que zero.")
        int quantity
) {
}
