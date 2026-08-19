package com.dro.modules.mail.api.dto.response;

import com.dro.modules.mail.domain.MailMessageType;

import java.time.LocalDateTime;
import java.util.UUID;

public record MailMessageSummaryResponse(
        UUID id,
        MailMessageType messageType,
        String actionType,
        String senderUsername,
        String recipientUsername,
        String subject,
        LocalDateTime createdAt,
        LocalDateTime readAt,
        boolean read
) {
}
