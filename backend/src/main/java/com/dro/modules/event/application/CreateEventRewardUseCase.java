package com.dro.modules.event.application;

import com.dro.modules.admin.api.dto.AdminEventRewardRequest;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.event.domain.EventReward;
import com.dro.modules.event.domain.EventRewardRecipientType;
import com.dro.modules.event.domain.EventRewardStatus;
import com.dro.modules.event.infra.EventRewardRepository;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.mail.application.CreateSystemMailMessageUseCase;
import com.dro.modules.mail.domain.MailMessageType;
import com.dro.modules.player.domain.Player;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateEventRewardUseCase {

    private final PlayerRepository playerRepository;
    private final ClanRepository clanRepository;
    private final EventRewardRepository eventRewardRepository;
    private final CreateSystemMailMessageUseCase createSystemMailMessageUseCase;

    @Transactional
    public EventRewardBatchResult execute(String token, AdminEventRewardRequest request) {
        UUID adminId = TokenExtractor.extractPlayerId(token);
        var admin = playerRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Administrador não encontrado."));
        if (admin.getUserType() != UserType.ADMIN) {
            throw new ForbiddenException("Somente administradores podem criar premiações.");
        }

        RewardValues values = validateReward(request);
        List<Player> recipients = resolveRecipients(request);
        LocalDateTime now = LocalDateTime.now();
        List<UUID> rewardIds = new ArrayList<>();
        int createdCount = 0;

        for (Player player : recipients) {
            UUID rewardId = UUID.randomUUID();
            int inserted = eventRewardRepository.insertIfAbsent(
                    rewardId,
                    player.getId(),
                    request.sourceType().trim(),
                    request.sourceId().trim(),
                    request.subject().trim(),
                    request.body().trim(),
                    values.bitsAmount(),
                    values.itemType(),
                    values.itemQuantity(),
                    EventRewardStatus.PENDING,
                    now,
                    now.plusDays(request.validityDays())
            );
            if (inserted == 0) {
                eventRewardRepository.findBySourceTypeAndSourceIdAndPlayerId(
                                request.sourceType().trim(), request.sourceId().trim(), player.getId())
                        .ifPresent(existing -> rewardIds.add(existing.getId()));
                continue;
            }

            createdCount++;
            rewardIds.add(rewardId);
            createSystemMailMessageUseCase.create(
                    MailMessageType.EVENT,
                    "EVENT_REWARD",
                    player.getId(),
                    rewardId,
                    "EVENT_REWARD_CLAIM",
                    request.subject().trim(),
                    request.body().trim(),
                    "event:reward:" + rewardId
            );
        }

        return new EventRewardBatchResult(
                createdCount,
                recipients.size() - createdCount,
                recipients.size(),
                rewardIds
        );
    }

    private RewardValues validateReward(AdminEventRewardRequest request) {
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
        return new RewardValues(bitsAmount, itemType, itemQuantity);
    }

    private List<Player> resolveRecipients(AdminEventRewardRequest request) {
        EventRewardRecipientType type = request.recipientType();
        if (type == null) {
            throw new ConflictException("Informe o modo de destinatário.");
        }

        Map<UUID, Player> unique = new LinkedHashMap<>();
        switch (type) {
            case PLAYER -> {
                if (request.playerUsername() == null || request.playerUsername().isBlank()) {
                    throw new ConflictException("Informe o jogador destinatário.");
                }
                Player player = playerRepository.findByUsernameIgnoreCase(request.playerUsername().trim())
                        .orElseThrow(() -> new NotFoundException("Jogador destinatário não encontrado."));
                unique.put(player.getId(), player);
            }
            case CLAN -> {
                if (request.clanId() == null || request.clanId().isBlank()) {
                    throw new ConflictException("Informe o clã destinatário.");
                }
                UUID clanId;
                try {
                    clanId = UUID.fromString(request.clanId().trim());
                } catch (IllegalArgumentException exception) {
                    throw new ConflictException("Identificador de clã inválido.");
                }
                Clan clan = clanRepository.findById(clanId)
                        .orElseThrow(() -> new NotFoundException("Clã destinatário não encontrado."));
                playerRepository.findByClanId(clan.getId()).forEach(player -> unique.put(player.getId(), player));
                if (unique.isEmpty()) {
                    throw new ConflictException("O clã selecionado não possui membros.");
                }
            }
            case PLAYERS -> {
                if (request.playerUsernames() == null || request.playerUsernames().isEmpty()) {
                    throw new ConflictException("Selecione pelo menos um jogador.");
                }
                for (String username : request.playerUsernames()) {
                    Player player = playerRepository.findByUsernameIgnoreCase(username.trim())
                            .orElseThrow(() -> new NotFoundException("Jogador não encontrado: " + username));
                    unique.put(player.getId(), player);
                }
            }
        }
        if (unique.size() > 100) {
            throw new ConflictException("A premiação pode alcançar no máximo 100 jogadores.");
        }
        return new ArrayList<>(unique.values());
    }

    private record RewardValues(int bitsAmount, String itemType, int itemQuantity) {
    }
}
