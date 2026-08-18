package com.dro.modules.mail.application;

import com.dro.modules.mail.infra.MailMessageRepository;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUnreadMailCountUseCase {

    private final MailMessageRepository mailMessageRepository;

    public long execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        return mailMessageRepository.countByRecipientIdAndRecipientDeletedFalseAndReadAtIsNull(playerId);
    }
}
