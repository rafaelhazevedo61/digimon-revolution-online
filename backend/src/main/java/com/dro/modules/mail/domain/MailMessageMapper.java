package com.dro.modules.mail.domain;

import com.dro.modules.mail.api.dto.response.MailMessageResponse;
import com.dro.modules.mail.api.dto.response.MailMessageSummaryResponse;

/**
 * Converte entidades de Correio em contratos de resposta da API.
 */
public final class MailMessageMapper {

    private MailMessageMapper() {
    }

    /**
     * Cria o resumo usado nas listas de Entrada e Enviadas.
     *
     * @param message mensagem persistida
     * @return resumo sem o corpo completo
     */
    public static MailMessageSummaryResponse toSummary(MailMessage message) {
        return new MailMessageSummaryResponse(
                message.getId(),
                message.getMessageType(),
                message.getActionType(),
                message.getSender() == null ? null : message.getSender().getUsername(),
                message.getRecipient().getUsername(),
                message.getSubject(),
                message.getCreatedAt(),
                message.getReadAt(),
                message.getReadAt() != null
        );
    }

    /**
     * Cria a resposta detalhada usada ao abrir uma mensagem.
     *
     * @param message mensagem persistida
     * @return resposta com corpo, remetente, destinatário e estado de leitura
     */
    public static MailMessageResponse toResponse(MailMessage message) {
        return new MailMessageResponse(
                message.getId(),
                message.getMessageType(),
                message.getActionType(),
                message.getSender() == null ? null : message.getSender().getUsername(),
                message.getRecipient().getUsername(),
                message.getSubject(),
                message.getBody(),
                message.getCreatedAt(),
                message.getReadAt(),
                message.getReadAt() != null
        );
    }
}
