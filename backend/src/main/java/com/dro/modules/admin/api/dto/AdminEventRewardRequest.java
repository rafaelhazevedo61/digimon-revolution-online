package com.dro.modules.admin.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AdminEventRewardRequest(
        @NotBlank(message = "Informe o jogador destinatário.")
        @Size(max = 30, message = "O nome do jogador deve ter no máximo 30 caracteres.")
        String playerUsername,
        @NotBlank(message = "Informe a origem da premiação.")
        @Size(max = 64, message = "A origem deve ter no máximo 64 caracteres.")
        String sourceType,
        @NotBlank(message = "Informe o identificador da origem.")
        @Size(max = 128, message = "O identificador da origem deve ter no máximo 128 caracteres.")
        String sourceId,
        @NotBlank(message = "O assunto é obrigatório.")
        @Size(max = 80, message = "O assunto deve ter no máximo 80 caracteres.")
        String subject,
        @NotBlank(message = "O texto da premiação é obrigatório.")
        @Size(max = 1000, message = "O texto deve ter no máximo 1.000 caracteres.")
        String body,
        @NotNull(message = "Informe a quantidade de Bits.")
        @PositiveOrZero(message = "A quantidade de Bits não pode ser negativa.")
        Integer bitsAmount,
        @Size(max = 50, message = "O tipo do item deve ter no máximo 50 caracteres.")
        String itemType,
        @NotNull(message = "Informe a quantidade do item.")
        @PositiveOrZero(message = "A quantidade do item não pode ser negativa.")
        Integer itemQuantity,
        @NotNull(message = "Informe a validade da premiação.")
        @Min(value = 1, message = "A validade mínima é de 1 dia.")
        @Max(value = 30, message = "A validade máxima é de 30 dias.")
        Integer validityDays
) {
}
