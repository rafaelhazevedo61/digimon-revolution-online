package com.dro.modules.mail.domain;

import com.dro.modules.mail.api.dto.response.MailMessageResponse;
import com.dro.modules.mail.api.dto.response.MailMessageSummaryResponse;

public final class MailMessageMapper {

    private MailMessageMapper() {
    }

    public static MailMessageSummaryResponse toSummary(MailMessage message) {
        return new MailMessageSummaryResponse(
                message.getId(),
                message.getMessageType(),
                message.getSender() == null ? null : message.getSender().getUsername(),
                message.getRecipient().getUsername(),
                message.getSubject(),
                message.getCreatedAt(),
                message.getReadAt(),
                message.getReadAt() != null
        );
    }

    public static MailMessageResponse toResponse(MailMessage message) {
        return new MailMessageResponse(
                message.getId(),
                message.getMessageType(),
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
