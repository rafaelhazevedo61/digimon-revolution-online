package com.dro.modules.event.application;

import com.dro.modules.admin.api.dto.AdminEventRewardRequest;
import com.dro.modules.event.domain.EventReward;
import com.dro.modules.event.domain.EventRewardStatus;
import com.dro.modules.event.infra.EventRewardRepository;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.mail.application.CreateSystemMailMessageUseCase;
import com.dro.modules.mail.domain.MailMessageType;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateEventRewardUseCase {

    private final PlayerRepository playerRepository;
    private final EventRewardRepository eventRewardRepository;
    private final CreateSystemMailMessageUseCase createSystemMailMessageUseCase;

    @Transactional
    public EventReward execute(String token, AdminEventRewardRequest request) {
        UUID adminId = TokenExtractor.extractPlayerId(token);
        var admin = playerRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Administrador não encontrado."));
        if (admin.getUserType() != UserType.ADMIN) {
            throw new ForbiddenException("Somente administradores podem criar premiações.");
        }

        int bitsAmount = request.bitsAmount() == null ? 0 : request.bitsAmount();
        int itemQuantity = request.itemQuantity() == null ? 0 : request.itemQuantity();
        String itemType = request.itemType() == null || request.itemType().isBlank()
                ? null : request.itemType().trim().toUpperCase();
        if (bitsAmount == 0 && itemQuantity == 0) {
            throw new ConflictException("A premiação precisa conter Bits ou um item.");
        }
        if (itemQuantity > 0 && itemType == null) {
            throw new ConflictException("Informe o tipo do item da premiação.");
        }
        if (itemQuantity == 0 && itemType != null) {
            throw new ConflictException("A quantidade do item precisa ser maior que zero.");
        }
        if (itemType != null) {
            try {
                ItemType.valueOf(itemType);
            } catch (IllegalArgumentException exception) {
                throw new ConflictException("Tipo de item inválido para a premiação.");
            }
        }

        var player = playerRepository.findByUsernameIgnoreCase(request.playerUsername().trim())
                .orElseThrow(() -> new NotFoundException("Jogador destinatário não encontrado."));
        String sourceType = request.sourceType().trim();
        String sourceId = request.sourceId().trim();
        if (eventRewardRepository.existsBySourceTypeAndSourceIdAndPlayerId(sourceType, sourceId, player.getId())) {
            throw new ConflictException("Esta premiação já foi criada para o jogador.");
        }

        LocalDateTime now = LocalDateTime.now();
        EventReward reward = EventReward.builder()
                .id(UUID.randomUUID())
                .player(player)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .subject(request.subject().trim())
                .body(request.body().trim())
                .bitsAmount(bitsAmount)
                .itemType(itemType)
                .itemQuantity(itemQuantity)
                .status(EventRewardStatus.PENDING)
                .createdAt(now)
                .expiresAt(now.plusDays(request.validityDays()))
                .build();
        eventRewardRepository.save(reward);

        String deliveryKey = "event:reward:" + reward.getId();
        createSystemMailMessageUseCase.create(
                MailMessageType.EVENT,
                "EVENT_REWARD",
                player.getId(),
                reward.getId(),
                "EVENT_REWARD_CLAIM",
                reward.getSubject(),
                reward.getBody(),
                deliveryKey
        );
        return reward;
    }
}
