package com.dro.modules.mail.application;

import com.dro.modules.mail.domain.MailMessage;
import com.dro.modules.mail.domain.MailMessageType;
import com.dro.modules.mail.domain.MailRules;
import com.dro.modules.mail.infra.MailMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateSystemMailMessageUseCase {

    private final MailMessageRepository mailMessageRepository;

    @Transactional
    public MailMessage createAuctionNotification(
            UUID recipientId,
            UUID sourceId,
            String actionType,
            String subject,
            String body,
            String deliveryKey
    ) {
        return create(
                MailMessageType.AUCTION,
                "AUCTION",
                recipientId,
                sourceId,
                actionType,
                subject,
                body,
                deliveryKey
        );
    }

    @Transactional
    public MailMessage create(
            MailMessageType messageType,
            String sourceType,
            UUID recipientId,
            UUID sourceId,
            String actionType,
            String subject,
            String body,
            String deliveryKey
    ) {
        validate(messageType, sourceType, recipientId, actionType, subject, body, deliveryKey);

        UUID messageId = UUID.randomUUID();
        mailMessageRepository.insertSystemMessage(
                messageId,
                recipientId,
                messageType.name(),
                subject.trim(),
                body.trim(),
                LocalDateTime.now(),
                sourceType,
                sourceId,
                actionType,
                null,
                deliveryKey
        );

        return mailMessageRepository.findByDeliveryKey(deliveryKey)
                .orElseThrow(() -> new IllegalStateException("System mail message could not be persisted"));
    }

    private void validate(
            MailMessageType messageType,
            String sourceType,
            UUID recipientId,
            String actionType,
            String subject,
            String body,
            String deliveryKey
    ) {
        if (messageType == null || messageType == MailMessageType.PLAYER) {
            throw new IllegalArgumentException("System mail requires a non-player message type");
        }
        if (sourceType == null || sourceType.isBlank()
                || sourceType.length() > MailRules.SOURCE_TYPE_MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid system mail source type");
        }
        if (recipientId == null) {
            throw new IllegalArgumentException("System mail recipient is required");
        }
        if (actionType != null && actionType.length() > MailRules.ACTION_TYPE_MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid system mail action type");
        }
        if (subject == null || subject.trim().isEmpty()
                || subject.trim().length() > MailRules.SUBJECT_MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid system mail subject");
        }
        if (body == null || body.trim().isEmpty()
                || body.trim().length() > MailRules.BODY_MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid system mail body");
        }
        if (deliveryKey == null || deliveryKey.isBlank()
                || deliveryKey.length() > MailRules.DELIVERY_KEY_MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid system mail delivery key");
        }
    }
}
