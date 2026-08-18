package com.dro.modules.mail.application;

import com.dro.modules.mail.api.dto.response.MailMessagePageResponse;
import com.dro.modules.mail.domain.MailMessageMapper;
import com.dro.modules.mail.domain.MailRules;
import com.dro.modules.mail.infra.MailMessageRepository;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListMailMessagesUseCase {

    private final MailMessageRepository mailMessageRepository;

    @Transactional(readOnly = true)
    public MailMessagePageResponse inbox(String token, int page, int size) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        PageRequest pageable = pageRequest(page, size);
        return toPage(mailMessageRepository
                .findByRecipientIdAndRecipientDeletedFalseOrderByCreatedAtDesc(playerId, pageable));
    }

    @Transactional(readOnly = true)
    public MailMessagePageResponse sent(String token, int page, int size) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        PageRequest pageable = pageRequest(page, size);
        return toPage(mailMessageRepository
                .findBySenderIdAndSenderDeletedFalseOrderByCreatedAtDesc(playerId, pageable));
    }

    private PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size <= 0 ? 20 : size, MailRules.MAX_PAGE_SIZE));
        return PageRequest.of(safePage, safeSize);
    }

    private MailMessagePageResponse toPage(Page<com.dro.modules.mail.domain.MailMessage> page) {
        return new MailMessagePageResponse(
                page.getContent().stream().map(MailMessageMapper::toSummary).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
