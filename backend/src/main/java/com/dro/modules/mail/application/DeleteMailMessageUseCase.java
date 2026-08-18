package com.dro.modules.mail.application;

import com.dro.modules.mail.domain.MailMessage;
import com.dro.modules.mail.infra.MailMessageRepository;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteMailMessageUseCase {

    private final MailMessageRepository mailMessageRepository;

    @Transactional
    public void execute(String token, UUID messageId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        MailMessage message = mailMessageRepository.findVisibleById(messageId, playerId)
                .orElseThrow(() -> new ConflictException("Mail message not found"));

        if (message.belongsToRecipient(playerId)) {
            message.setRecipientDeleted(true);
        } else if (message.belongsToSender(playerId)) {
            message.setSenderDeleted(true);
        } else {
            throw new ConflictException("Mail message not found");
        }

        mailMessageRepository.save(message);
    }
}
