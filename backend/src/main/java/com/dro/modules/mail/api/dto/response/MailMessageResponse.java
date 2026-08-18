package com.dro.modules.mail.api.dto.response;

import com.dro.modules.mail.domain.MailMessageType;

import java.time.LocalDateTime;
import java.util.UUID;

public record MailMessageResponse(
        UUID id,
        MailMessageType messageType,
        String senderUsername,
        String recipientUsername,
        String subject,
        String body,
        LocalDateTime createdAt,
        LocalDateTime readAt,
        boolean read
) {
}
