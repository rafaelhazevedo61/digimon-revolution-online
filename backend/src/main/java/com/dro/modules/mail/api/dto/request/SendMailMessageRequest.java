package com.dro.modules.mail.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados necessários para enviar uma mensagem de texto entre jogadores.
 *
 * <p>Mensagens comuns não aceitam anexos, Bits ou itens. Os limites de assunto
 * e corpo também são aplicados pelo caso de uso antes da persistência.</p>
 *
 * @param recipientUsername username do jogador destinatário
 * @param subject assunto com até 80 caracteres
 * @param body texto com até 1.000 caracteres
 */
public record SendMailMessageRequest(
        @NotBlank(message = "Recipient username is required")
        @Size(max = 30, message = "Recipient username must be at most 30 characters")
        String recipientUsername,

        @NotBlank(message = "Subject is required")
        @Size(max = 80, message = "Subject must be at most 80 characters")
        String subject,

        @NotBlank(message = "Message body is required")
        @Size(max = 1000, message = "Message body must be at most 1000 characters")
        String body
) {
}
