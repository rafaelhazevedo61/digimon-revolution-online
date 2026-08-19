package com.dro.modules.mail.api.dto.response;

import com.dro.modules.mail.domain.MailMessageType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representação detalhada de uma mensagem do Correio.
 *
 * @param id identificador da mensagem
 * @param messageType categoria da mensagem
 * @param actionType ação pendente, quando houver
 * @param senderUsername username do remetente, nulo para mensagens do sistema
 * @param recipientUsername username do destinatário
 * @param subject assunto
 * @param body corpo completo da mensagem
 * @param createdAt instante de criação
 * @param readAt instante em que o destinatário abriu a mensagem
 * @param read indica se a mensagem já foi aberta
 */
public record MailMessageResponse(
        UUID id,
        MailMessageType messageType,
        String actionType,
        String senderUsername,
        String recipientUsername,
        String subject,
        String body,
        LocalDateTime createdAt,
        LocalDateTime readAt,
        boolean read
) {
}
