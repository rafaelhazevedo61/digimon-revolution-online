package com.dro.modules.event.application;

import com.dro.modules.admin.api.dto.AdminEventRewardRequest;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.event.domain.EventReward;
import com.dro.modules.event.domain.EventRewardRecipientType;
import com.dro.modules.event.domain.EventRewardStatus;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Cria premiações de eventos e a mensagem individual correspondente no Correio.
 *
 * <p>O caso de uso resolve os destinatários no momento da operação, remove
 * duplicidades por jogador e insere cada premiação de forma idempotente. A chave
 * {@code sourceType + sourceId + player} impede que o reprocessamento do mesmo
 * evento entregue uma segunda recompensa ao mesmo jogador.</p>
 *
 * <p>A operação exige um usuário {@code ADMIN} por meio do
 * {@code AdminAuthInterceptor} e é transacional: a premiação e sua mensagem de
 * Correio são criadas dentro do mesmo fluxo persistente.</p>
 */
@Service
public class CreateEventRewardUseCase {
    private final PlayerRepository playerRepository;
    private final ClanRepository clanRepository;
    private final EventRewardRepository eventRewardRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final CreateSystemMailMessageUseCase createSystemMailMessageUseCase;

    /**
     * Cria as premiações para os destinatários definidos na solicitação.
     *
     * <p>Destinatários que já possuem a mesma origem são ignorados e aparecem
     * no resultado por username. Um novo {@code sourceId} representa uma nova
     * origem e pode gerar uma nova premiação.</p>
     *
     * @param request conteúdo, validade e seleção dos destinatários
     * @return contagens, identificadores criados e usernames ignorados
     * @throws NotFoundException quando um jogador ou clã destinatário não existe
     * @throws ConflictException quando os valores ou destinatários são inválidos
     */
    @Transactional
    public EventRewardBatchResult execute(AdminEventRewardRequest request) {
        RewardValues values = validateReward(request);
        List<Player> recipients = resolveRecipients(request);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(request.validityDays());
        String messageBody = EventRewardMessageText.pendingBody(request.body(), values.bitsAmount(), values.itemType(), values.itemLabel(), values.itemQuantity(), expiresAt);
        List<UUID> rewardIds = new ArrayList<>();
        List<String> skippedUsernames = new ArrayList<>();
        int createdCount = 0;
        for (Player player : recipients) {
            UUID rewardId = UUID.randomUUID();
            int inserted = eventRewardRepository.insertIfAbsent(rewardId, player.getId(), request.sourceType().trim(), request.sourceId().trim(), request.subject().trim(), messageBody, values.bitsAmount(), values.itemType(), values.itemDefinitionCode(), values.itemQuantity(), EventRewardStatus.PENDING.name(), now, expiresAt);
            if (inserted == 0) {
                eventRewardRepository.findBySourceTypeAndSourceIdAndPlayerId(request.sourceType().trim(), request.sourceId().trim(), player.getId()).ifPresent(existing -> rewardIds.add(existing.getId()));
                skippedUsernames.add(player.getUsername());
                continue;
            }
            createdCount++;
            rewardIds.add(rewardId);
            createSystemMailMessageUseCase.create(MailMessageType.EVENT, "EVENT_REWARD", player.getId(), rewardId, "EVENT_REWARD_CLAIM", request.subject().trim(), messageBody, "event:reward:" + rewardId);
        }
        return new EventRewardBatchResult(createdCount, recipients.size() - createdCount, recipients.size(), rewardIds, skippedUsernames);
    }

    /**
     * Valida e normaliza Bits e item antes de persistir a premiação.
     *
     * @param request solicitação recebida do painel administrativo
     * @return valores normalizados para a criação da premiação
     * @throws ConflictException quando a premiação não possui conteúdo válido
     */
    private RewardValues validateReward(AdminEventRewardRequest request) {
        int bitsAmount = request.bitsAmount() == null ? 0 : request.bitsAmount();
        int itemQuantity = request.itemQuantity() == null ? 0 : request.itemQuantity();
        String itemType = request.itemType() == null || request.itemType().isBlank() ? null : request.itemType().trim().toUpperCase();
        String itemDefinitionCode = request.itemDefinitionCode() == null || request.itemDefinitionCode().isBlank() ? null : request.itemDefinitionCode().trim().toUpperCase();
        String itemLabel = null;
        if (bitsAmount == 0 && itemQuantity == 0) {
            throw new ConflictException("A premiação precisa conter Bits ou um item.");
        }
        if (itemQuantity > 0 && itemType == null && itemDefinitionCode == null) {
            throw new ConflictException("Informe o item da premiação.");
        }
        if (itemQuantity == 0 && (itemType != null || itemDefinitionCode != null)) {
            throw new ConflictException("A quantidade do item precisa ser maior que zero.");
        }
        if (itemType != null && itemDefinitionCode != null) {
            throw new ConflictException("Informe o item pelo catálogo ou pelo tipo legado, não ambos.");
        }
        if (itemDefinitionCode != null) {
            ItemDefinition definition = itemDefinitionRepository.findByCode(itemDefinitionCode)
                    .orElseThrow(() -> new ConflictException("Definição de item não encontrada: " + itemDefinitionCode));
            itemType = resolveItemType(definition).name();
            itemLabel = definition.getName();
        } else if (itemType != null) {
            try {
                ItemType.valueOf(itemType);
            } catch (IllegalArgumentException exception) {
                throw new ConflictException("Tipo de item inválido para a premiação.");
            }
        }
        return new RewardValues(bitsAmount, itemType, itemDefinitionCode, itemLabel, itemQuantity);
    }

    /**
     * Expande o modo de destinatário em uma lista única de jogadores.
     *
     * <p>No modo {@code CLAN}, são considerados os membros vinculados ao clã no
     * momento do envio. No modo {@code PLAYERS}, a lista final não pode exceder
     * 100 jogadores. No modo {@code ALL_PLAYERS}, são consideradas todas as
     * contas do tipo {@code PLAYER} existentes no momento do envio.</p>
     *
     * @param request solicitação com o modo e os identificadores de destino
     * @return jogadores únicos que serão processados pelo lote
     * @throws ConflictException quando o modo, o identificador ou o limite são inválidos
     * @throws NotFoundException quando um destinatário ou clã não existe
     */
    private ItemType resolveItemType(ItemDefinition definition) {
        if ("CHEST".equalsIgnoreCase(definition.getCategory())) {
            return ItemType.LOOT_CHEST;
        }
        try {
            return ItemType.valueOf(definition.getCode());
        } catch (IllegalArgumentException exception) {
            if ("EVOLUTION_MATERIAL".equalsIgnoreCase(definition.getCategory())) {
                return ItemType.EVOLUTION_MATERIAL;
            }
            throw new ConflictException("Definição de item não pode ser usada em premiações: " + definition.getCode());
        }
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


    private record RewardValues(int bitsAmount, String itemType, String itemDefinitionCode, String itemLabel, int itemQuantity) {
    }

    public CreateEventRewardUseCase(final PlayerRepository playerRepository, final ClanRepository clanRepository, final EventRewardRepository eventRewardRepository, final ItemDefinitionRepository itemDefinitionRepository, final CreateSystemMailMessageUseCase createSystemMailMessageUseCase) {
        this.playerRepository = playerRepository;
        this.clanRepository = clanRepository;
        this.eventRewardRepository = eventRewardRepository;
        this.itemDefinitionRepository = itemDefinitionRepository;
        this.createSystemMailMessageUseCase = createSystemMailMessageUseCase;
    }
}
