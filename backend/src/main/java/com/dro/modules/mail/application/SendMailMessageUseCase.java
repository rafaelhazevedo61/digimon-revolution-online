package com.dro.modules.mail.application;

import com.dro.modules.mail.api.dto.request.SendMailMessageRequest;
import com.dro.modules.mail.api.dto.response.MailMessageResponse;
import com.dro.modules.mail.domain.MailMessage;
import com.dro.modules.mail.domain.MailMessageMapper;
import com.dro.modules.mail.domain.MailMessageType;
import com.dro.modules.mail.domain.MailRules;
import com.dro.modules.mail.infra.MailMessageRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SendMailMessageUseCase {

    private final PlayerRepository playerRepository;
    private final MailMessageRepository mailMessageRepository;

    @Transactional
    public MailMessageResponse execute(String token, SendMailMessageRequest request) {
        UUID senderId = TokenExtractor.extractPlayerId(token);
        Player sender = playerRepository.findById(senderId)
                .orElseThrow(() -> new ConflictException("Sender player not found"));

        String recipientUsername = request.recipientUsername().trim();
        Player recipient = playerRepository.findByUsernameIgnoreCase(recipientUsername)
                .orElseThrow(() -> new BadRequestException("Recipient player not found"));

        if (sender.getId().equals(recipient.getId())) {
            throw new BadRequestException("You cannot send a message to yourself");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rateWindow = now.minusMinutes(1);
        if (mailMessageRepository.countBySenderIdAndCreatedAtAfter(senderId, rateWindow)
                >= MailRules.MAX_MESSAGES_PER_MINUTE) {
            throw new UnprocessableException("Too many messages sent. Try again in a moment");
        }

        String subject = request.subject().trim();
        String body = request.body().trim();
        if (subject.isEmpty() || body.isEmpty()) {
            throw new BadRequestException("Subject and message body cannot be empty");
        }

        MailMessage message = MailMessage.builder()
                .id(UUID.randomUUID())
                .sender(sender)
                .recipient(recipient)
                .messageType(MailMessageType.PLAYER)
                .subject(subject)
                .body(body)
                .createdAt(now)
                .build();

        return MailMessageMapper.toResponse(mailMessageRepository.save(message));
    }
}
