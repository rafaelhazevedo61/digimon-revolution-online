package com.dro.modules.mail.application;

import com.dro.modules.event.infra.EventRewardRepository;
import com.dro.modules.mail.api.dto.response.BulkMailDeleteResponse;
import com.dro.modules.mail.domain.MailMessage;
import com.dro.modules.mail.infra.MailMessageRepository;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DeleteAllMailMessagesUseCase {
    private final MailMessageRepository mailMessageRepository;
    private final EventRewardRepository eventRewardRepository;

    @Transactional
    public BulkMailDeleteResponse execute(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        List<MailMessage> messages = mailMessageRepository.findByRecipientIdAndRecipientDeletedFalse(playerId);
        int deleted = 0;
        int preserved = 0;
        for (MailMessage message : messages) {
            if (hasClaimableReward(message, playerId)) {
                preserved++;
            } else {
                message.setRecipientDeleted(true);
                deleted++;
            }
        }
        mailMessageRepository.saveAll(messages);
        return new BulkMailDeleteResponse(deleted, preserved);
    }

    private boolean hasClaimableReward(MailMessage message, UUID playerId) {
        if (!playerId.equals(message.getRecipient().getId())
                || !"EVENT_REWARD".equals(message.getSourceType())
                || !"EVENT_REWARD_CLAIM".equals(message.getActionType())
                || message.getSourceId() == null) return false;
        return eventRewardRepository.findByIdAndPlayerIdForUpdate(message.getSourceId(), playerId)
                .map(reward -> reward.isPendingAt(LocalDateTime.now()))
                .orElse(false);
    }

    public DeleteAllMailMessagesUseCase(MailMessageRepository mailMessageRepository, EventRewardRepository eventRewardRepository) {
        this.mailMessageRepository = mailMessageRepository;
        this.eventRewardRepository = eventRewardRepository;
    }
}
