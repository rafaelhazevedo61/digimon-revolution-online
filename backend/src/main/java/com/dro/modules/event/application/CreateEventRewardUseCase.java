package com.dro.modules.event.application;

import com.dro.modules.admin.api.dto.AdminEventRewardItemRequest;
import com.dro.modules.admin.api.dto.AdminEventRewardRequest;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.event.domain.EventReward;
import com.dro.modules.event.domain.EventRewardItem;
import com.dro.modules.event.domain.EventRewardRecipientType;
import com.dro.modules.event.domain.EventRewardStatus;
import com.dro.modules.event.infra.EventRewardItemRepository;
import com.dro.modules.event.infra.EventRewardRepository;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.mail.application.CreateSystemMailMessageUseCase;
import com.dro.modules.mail.domain.MailMessageType;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Cria premiações de eventos e a mensagem individual correspondente no Correio.
 *
 * <p>O caso de uso resolve os destinatários no momento da operação, remove
 * duplicidades por jogador e insere cada premiação de forma idempotente. A chave
 * {@code sourceType + sourceId + player} impede que o reprocessamento do mesmo
 * evento entregue uma segunda recompensa ao mesmo jogador.</p>
 *
 * <p>A premiação aceita uma coleção de até dez itens distintos, cada um com sua
 * quantidade. Os campos antigos de item único continuam aceitos para manter
 * compatibilidade com integrações já existentes.</p>
 */
@Service
public class CreateEventRewardUseCase {
    private static final int MAX_ITEMS = 10;

    private final PlayerRepository playerRepository;
    private final ClanRepository clanRepository;
    private final EventRewardRepository eventRewardRepository;
    private final EventRewardItemRepository eventRewardItemRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final CreateSystemMailMessageUseCase createSystemMailMessageUseCase;

    @Transactional
    public EventRewardBatchResult execute(AdminEventRewardRequest request) {
        RewardValues values = validateReward(request);
        List<Player> recipients = resolveRecipients(request);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(request.validityDays());
        String messageBody = EventRewardMessageText.pendingBody(request.body(), values.bitsAmount(), values.items().stream().map(this::toMessageItem).toList(), expiresAt);
        RewardItemValues primaryItem = values.items().isEmpty() ? null : values.items().get(0);
        List<UUID> rewardIds = new ArrayList<>();
        List<String> skippedUsernames = new ArrayList<>();
        int createdCount = 0;
        for (Player player : recipients) {
            UUID rewardId = UUID.randomUUID();
            int inserted = eventRewardRepository.insertIfAbsent(
                    rewardId,
                    player.getId(),
                    request.sourceType().trim(),
                    request.sourceId().trim(),
                    request.subject().trim(),
                    messageBody,
                    values.bitsAmount(),
                    primaryItem == null ? null : primaryItem.itemType(),
                    primaryItem == null ? null : primaryItem.itemDefinitionCode(),
                    primaryItem == null ? 0 : primaryItem.quantity(),
                    EventRewardStatus.PENDING.name(),
                    now,
                    expiresAt
            );
            if (inserted == 0) {
                eventRewardRepository.findBySourceTypeAndSourceIdAndPlayerId(request.sourceType().trim(), request.sourceId().trim(), player.getId()).ifPresent(existing -> rewardIds.add(existing.getId()));
                skippedUsernames.add(player.getUsername());
                continue;
            }
            if (!values.items().isEmpty()) {
                eventRewardItemRepository.saveAll(values.items().stream()
                        .map(item -> EventRewardItem.builder()
                                .id(UUID.randomUUID())
                                .eventRewardId(rewardId)
                                .itemType(item.itemType())
                                .itemDefinitionCode(item.itemDefinitionCode())
                                .itemQuantity(item.quantity())
                                .position(item.position())
                                .build())
                        .toList());
            }
            createdCount++;
            rewardIds.add(rewardId);
            createSystemMailMessageUseCase.create(MailMessageType.EVENT, "EVENT_REWARD", player.getId(), rewardId, "EVENT_REWARD_CLAIM", request.subject().trim(), messageBody, "event:reward:" + rewardId);
        }
        return new EventRewardBatchResult(createdCount, recipients.size() - createdCount, recipients.size(), rewardIds, skippedUsernames);
    }

    private EventRewardMessageText.ItemLabel toMessageItem(RewardItemValues item) {
        return new EventRewardMessageText.ItemLabel(item.itemType(), item.itemLabel(), item.quantity());
    }

    /** Valida Bits e a coleção moderna ou os campos legados de item único. */
    private RewardValues validateReward(AdminEventRewardRequest request) {
        int bitsAmount = request.bitsAmount() == null ? 0 : request.bitsAmount();
        int itemQuantity = request.itemQuantity() == null ? 0 : request.itemQuantity();
        String itemType = normalize(request.itemType());
        String itemDefinitionCode = normalize(request.itemDefinitionCode());
        List<AdminEventRewardItemRequest> requestedItems = request.items() == null ? List.of() : request.items();

        if (bitsAmount < 0 || itemQuantity < 0) {
            throw new ConflictException("A quantidade da premiação não pode ser negativa.");
        }
        boolean hasLegacyItemFields = itemType != null || itemDefinitionCode != null || itemQuantity > 0;
        List<RewardItemValues> items;
        if (!requestedItems.isEmpty()) {
            if (hasLegacyItemFields) {
                throw new ConflictException("Informe os itens pela lista moderna ou pelo formato legado, não ambos.");
            }
            items = normalizeItems(requestedItems);
        } else {
            items = normalizeLegacyItem(itemType, itemDefinitionCode, itemQuantity);
        }
        if (bitsAmount == 0 && items.isEmpty()) {
            throw new ConflictException("A premiação precisa conter Bits ou pelo menos um item.");
        }
        return new RewardValues(bitsAmount, items);
    }

    private List<RewardItemValues> normalizeItems(List<AdminEventRewardItemRequest> requestedItems) {
        if (requestedItems.size() > MAX_ITEMS) {
            throw new ConflictException("A premiação pode conter no máximo " + MAX_ITEMS + " itens diferentes.");
        }
        Set<String> codes = new LinkedHashSet<>();
        List<RewardItemValues> items = new ArrayList<>();
        for (int index = 0; index < requestedItems.size(); index++) {
            AdminEventRewardItemRequest requestItem = requestedItems.get(index);
            if (requestItem == null || requestItem.itemDefinitionCode() == null || requestItem.itemDefinitionCode().isBlank()) {
                throw new ConflictException("Informe o código da definição do item na posição " + (index + 1) + ".");
            }
            if (requestItem.quantity() <= 0) {
                throw new ConflictException("A quantidade do item na posição " + (index + 1) + " deve ser maior que zero.");
            }
            String code = requestItem.itemDefinitionCode().trim().toUpperCase();
            if (!codes.add(code)) {
                throw new ConflictException("O item " + code + " foi informado mais de uma vez.");
            }
            ItemDefinition definition = itemDefinitionRepository.findByCode(code)
                    .orElseThrow(() -> new ConflictException("Definição de item não encontrada: " + code));
            items.add(new RewardItemValues(resolveItemType(definition).name(), code, definition.getName(), requestItem.quantity(), index));
        }
        return items;
    }

    private List<RewardItemValues> normalizeLegacyItem(String itemType, String itemDefinitionCode, int itemQuantity) {
        if (itemQuantity == 0 && (itemType != null || itemDefinitionCode != null)) {
            throw new ConflictException("A quantidade do item precisa ser maior que zero.");
        }
        if (itemQuantity == 0) {
            return List.of();
        }
        if (itemType != null && itemDefinitionCode != null) {
            throw new ConflictException("Informe o item pelo catálogo ou pelo tipo legado, não ambos.");
        }
        if (itemDefinitionCode != null) {
            ItemDefinition definition = itemDefinitionRepository.findByCode(itemDefinitionCode)
                    .orElseThrow(() -> new ConflictException("Definição de item não encontrada: " + itemDefinitionCode));
            return List.of(new RewardItemValues(resolveItemType(definition).name(), itemDefinitionCode, definition.getName(), itemQuantity, 0));
        }
        try {
            ItemType type = ItemType.valueOf(itemType);
            return List.of(new RewardItemValues(type.name(), null, null, itemQuantity, 0));
        } catch (IllegalArgumentException exception) {
            throw new ConflictException("Tipo de item inválido para a premiação.");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }

    private ItemType resolveItemType(ItemDefinition definition) {
        if ("CHEST".equalsIgnoreCase(definition.getCategory())) {
            return ItemType.LOOT_CHEST;
        }
        try {
            return ItemType.valueOf(definition.getCode());
        } catch (IllegalArgumentException exception) {
            return ItemType.EVOLUTION_MATERIAL;
        }
    }

    /** Expande o modo de destinatário em uma lista única de jogadores. */
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
                Player player = playerRepository.findByUsernameIgnoreCase(request.playerUsername().trim()).orElseThrow(() -> new NotFoundException("Jogador destinatário não encontrado."));
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
                Clan clan = clanRepository.findById(clanId).orElseThrow(() -> new NotFoundException("Clã destinatário não encontrado."));
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
                    Player player = playerRepository.findByUsernameIgnoreCase(username.trim()).orElseThrow(() -> new NotFoundException("Jogador não encontrado: " + username));
                    unique.put(player.getId(), player);
                }
            }
            case ALL_PLAYERS -> playerRepository.findByUserTypeOrderByUsernameAsc(UserType.PLAYER)
                    .forEach(player -> unique.put(player.getId(), player));
        }
        if (unique.isEmpty()) {
            throw new ConflictException("Nenhum jogador elegível foi encontrado.");
        }
        if (type != EventRewardRecipientType.ALL_PLAYERS && unique.size() > 100) {
            throw new ConflictException("A premiação pode alcançar no máximo 100 jogadores.");
        }
        return new ArrayList<>(unique.values());
    }

    private record RewardItemValues(String itemType, String itemDefinitionCode, String itemLabel, int quantity, int position) {
    }

    private record RewardValues(int bitsAmount, List<RewardItemValues> items) {
    }

    public CreateEventRewardUseCase(final PlayerRepository playerRepository, final ClanRepository clanRepository, final EventRewardRepository eventRewardRepository, final EventRewardItemRepository eventRewardItemRepository, final ItemDefinitionRepository itemDefinitionRepository, final CreateSystemMailMessageUseCase createSystemMailMessageUseCase) {
        this.playerRepository = playerRepository;
        this.clanRepository = clanRepository;
        this.eventRewardRepository = eventRewardRepository;
        this.eventRewardItemRepository = eventRewardItemRepository;
        this.itemDefinitionRepository = itemDefinitionRepository;
        this.createSystemMailMessageUseCase = createSystemMailMessageUseCase;
    }
}
