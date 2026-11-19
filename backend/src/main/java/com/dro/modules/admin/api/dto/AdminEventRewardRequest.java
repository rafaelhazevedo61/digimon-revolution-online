package com.dro.modules.admin.api.dto;

import com.dro.modules.event.domain.EventRewardRecipientType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Solicitação administrativa para criar premiações de eventos em lote.
 *
 * <p>O modo {@code PLAYER} usa {@code playerUsername}, o modo {@code CLAN}
 * usa {@code clanId}, o modo {@code PLAYERS} usa {@code playerUsernames} e o
 * modo {@code ALL_PLAYERS} alcança todos os jogadores do servidor no momento
 * do envio. A combinação de {@code sourceType}, {@code sourceId} e jogador
 * define a idempotência da entrega.</p>
 *
 * @param recipientType estratégia de expansão dos destinatários
 * @param playerUsername username usado no modo individual
 * @param clanId identificador do clã usado no modo de clã
 * @param playerUsernames lista usada no modo manual, com no máximo 100 nomes
 * @param sourceType tipo estável da origem do evento
 * @param sourceId identificador estável da origem do evento
 * @param subject assunto da mensagem de Correio
 * @param body texto personalizado da premiação
 * @param bitsAmount quantidade de Bits, podendo ser zero
 * @param itemType tipo legado do item, quando houver item
 * @param itemDefinitionCode código específico da definição no catálogo, quando selecionado
 * @param items itens do catálogo, cada um com sua quantidade independente
 * @param itemQuantity quantidade do item legado, podendo ser zero
 * @param validityDays validade entre 1 e 30 dias
 */
public record AdminEventRewardRequest(
        @NotNull(message = "Informe o modo de destinatário.")
        EventRewardRecipientType recipientType,
        @Size(max = 30, message = "O nome do jogador deve ter no máximo 30 caracteres.")
        String playerUsername,
        @Size(max = 36, message = "O identificador do clã deve ter no máximo 36 caracteres.")
        String clanId,
        @Size(max = 100, message = "A lista pode conter no máximo 100 jogadores.")
        List<@NotBlank(message = "A lista contém um nome de jogador vazio.") @Size(max = 30, message = "O nome do jogador deve ter no máximo 30 caracteres.") String> playerUsernames,
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
        @Size(max = 128, message = "O código da definição deve ter no máximo 128 caracteres.")
        String itemDefinitionCode,
        @Size(max = 10, message = "A premiação pode conter no máximo 10 itens diferentes.")
        List<@Valid AdminEventRewardItemRequest> items,
        @NotNull(message = "Informe a quantidade do item.")
        @PositiveOrZero(message = "A quantidade do item não pode ser negativa.")
        Integer itemQuantity,
        @NotNull(message = "Informe a validade da premiação.")
        @Min(value = 1, message = "A validade mínima é de 1 dia.")
        @Max(value = 30, message = "A validade máxima é de 30 dias.")
        Integer validityDays
) {
}
