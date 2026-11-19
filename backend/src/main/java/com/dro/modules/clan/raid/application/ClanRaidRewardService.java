package com.dro.modules.clan.raid.application;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.clan.raid.api.dto.response.ClanRaidRewardResponse;
import com.dro.modules.clan.raid.domain.ClanRaid;
import com.dro.modules.clan.raid.domain.ClanRaidAttack;
import com.dro.modules.clan.raid.domain.ClanRaidReward;
import com.dro.modules.clan.raid.domain.ClanRaidRewardType;
import com.dro.modules.clan.raid.infra.ClanRaidAttackRepository;
import com.dro.modules.clan.raid.infra.ClanRaidRewardRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.shared.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Coordena as recompensas do ciclo de uma Incursão de Clã.
 *
 * <p>O maior dano é calculado pela soma de todos os ataques do jogador na
 * incursão. A concessão é registrada com uma chave de evento única para que
 * reprocessamentos não entreguem o mesmo baú duas vezes.</p>
 */
@Service
public class ClanRaidRewardService {
    private final ClanRaidAttackRepository clanRaidAttackRepository;
    private final ClanRaidRewardRepository clanRaidRewardRepository;
    private final AddItemUseCase addItemUseCase;

    /**
     * Concede o Baú da tentativa e, quando o ataque derrota o chefe, os dois
     * Baús especiais de encerramento.
     */
    @Transactional
    public List<ClanRaidRewardResponse> grant(
            BossDefinitionEntity boss,
            ClanRaid raid,
            ClanRaidAttack attack,
            boolean defeated
    ) {
        List<ClanRaidRewardResponse> rewards = new ArrayList<>();
        rewards.add(grantOne(boss, raid, attack, attack, ClanRaidRewardType.ATTEMPT));
        if (defeated) {
            List<ClanRaidAttack> attacks = new ArrayList<>(
                    clanRaidAttackRepository.findByClanRaidIdOrderByCreatedAtDesc(raid.getId())
            );
            if (attacks.stream().noneMatch(item -> item.getId().equals(attack.getId()))) {
                attacks.add(attack);
            }
            ClanRaidAttack topDamageAttack = findTopDamageAttack(attacks);
            rewards.add(grantOne(boss, raid, attack, topDamageAttack, ClanRaidRewardType.TOP_DAMAGE));
            rewards.add(grantOne(boss, raid, attack, attack, ClanRaidRewardType.FINAL_BLOW));
        }
        return rewards;
    }

    @Transactional(readOnly = true)
    public List<ClanRaidRewardResponse> findBySourceAttackId(UUID sourceAttackId) {
        return clanRaidRewardRepository.findBySourceAttackIdOrderByRewardTypeAsc(sourceAttackId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClanRaidRewardResponse> findPlayerRewards(UUID clanRaidId, UUID playerId) {
        return clanRaidRewardRepository
                .findByClanRaidIdAndRecipientPlayerIdOrderByCreatedAtAsc(clanRaidId, playerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ClanRaidRewardResponse grantOne(
            BossDefinitionEntity boss,
            ClanRaid raid,
            ClanRaidAttack sourceAttack,
            ClanRaidAttack recipientAttack,
            ClanRaidRewardType rewardType
    ) {
        String eventKey = "clan-raid:" + raid.getId() + ":" + rewardType.getCode() + ":" + sourceAttack.getId();
        ClanRaidReward existing = clanRaidRewardRepository.findByEventKey(eventKey).orElse(null);
        if (existing != null) {
            return toResponse(existing);
        }

        ChestDefinitionEntity chest = resolveChest(boss, rewardType);
        addItemUseCase.addMaterial(recipientAttack.getDigimonId(), chest.getItemDefinition(), 1);
        ClanRaidReward reward = ClanRaidReward.builder()
                .id(UUID.randomUUID())
                .clanRaidId(raid.getId())
                .sourceAttackId(sourceAttack.getId())
                .recipientPlayerId(recipientAttack.getPlayerId())
                .recipientDigimonId(recipientAttack.getDigimonId())
                .chestDefinition(chest)
                .rewardType(rewardType)
                .eventKey(eventKey)
                .createdAt(Instant.now())
                .build();
        return toResponse(clanRaidRewardRepository.save(reward));
    }

    private ChestDefinitionEntity resolveChest(BossDefinitionEntity boss, ClanRaidRewardType rewardType) {
        ChestDefinitionEntity chest = boss.chestForClanRaidReward(rewardType);
        String rewardLabel = switch (rewardType) {
            case ATTEMPT -> "tentativa";
            case TOP_DAMAGE -> "maior dano acumulado";
            case FINAL_BLOW -> "golpe final";
        };
        if (chest == null) {
            throw new ConflictException("Incursão de Clã não possui Baú configurado para " + rewardLabel + ".");
        }
        if (!chest.isActive()) {
            throw new ConflictException("Baú de recompensa da Incursão de Clã está inativo: " + chest.getCode());
        }
        if (chest.getLootTable() == null || !chest.getLootTable().isActive()) {
            throw new ConflictException("Loot Table do Baú da Incursão de Clã está inativa: " + chest.getCode());
        }
        return chest;
    }

    private ClanRaidAttack findTopDamageAttack(List<ClanRaidAttack> attacks) {
        Map<UUID, List<ClanRaidAttack>> byPlayer = attacks.stream()
                .collect(Collectors.groupingBy(ClanRaidAttack::getPlayerId, LinkedHashMap::new, Collectors.toList()));
        return byPlayer.values().stream()
                .map(playerAttacks -> new PlayerDamageSummary(
                        playerAttacks.get(0).getPlayerId(),
                        playerAttacks.stream().mapToLong(ClanRaidAttack::getDamage).sum(),
                        playerAttacks.stream().map(ClanRaidAttack::getCreatedAt).min(Comparator.naturalOrder()).orElseThrow(),
                        playerAttacks.stream()
                                .max(Comparator.comparing(ClanRaidAttack::getCreatedAt).thenComparing(ClanRaidAttack::getId))
                                .orElseThrow()
                ))
                .sorted(Comparator.comparingLong(PlayerDamageSummary::totalDamage)
                        .reversed()
                        .thenComparing(PlayerDamageSummary::firstAttackAt)
                        .thenComparing(summary -> summary.playerId().toString()))
                .map(PlayerDamageSummary::recipientAttack)
                .findFirst()
                .orElseThrow(() -> new ConflictException("Não foi possível identificar participante da Incursão de Clã derrotada"));
    }

    private ClanRaidRewardResponse toResponse(ClanRaidReward reward) {
        return new ClanRaidRewardResponse(
                reward.getRewardType().getCode(),
                reward.getChestDefinition().getCode(),
                reward.getChestDefinition().getName()
        );
    }

    private record PlayerDamageSummary(
            UUID playerId,
            long totalDamage,
            Instant firstAttackAt,
            ClanRaidAttack recipientAttack
    ) {
    }

    public ClanRaidRewardService(
            ClanRaidAttackRepository clanRaidAttackRepository,
            ClanRaidRewardRepository clanRaidRewardRepository,
            AddItemUseCase addItemUseCase
    ) {
        this.clanRaidAttackRepository = clanRaidAttackRepository;
        this.clanRaidRewardRepository = clanRaidRewardRepository;
        this.addItemUseCase = addItemUseCase;
    }
}
