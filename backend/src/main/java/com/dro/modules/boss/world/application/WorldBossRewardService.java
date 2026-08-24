package com.dro.modules.boss.world.application;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.world.api.dto.response.WorldBossRewardResponse;
import com.dro.modules.boss.world.domain.WorldBossAttack;
import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.domain.WorldBossReward;
import com.dro.modules.boss.world.domain.WorldBossRewardType;
import com.dro.modules.boss.world.infra.WorldBossAttackRepository;
import com.dro.modules.boss.world.infra.WorldBossRewardRepository;
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
 * Coordena as recompensas do ciclo diário do Boss Mundial.
 *
 * <p>O maior dano é calculado pela soma de todos os ataques do jogador na
 * instância. Como a lista de ataques representa os participantes, o vencedor
 * do ranking é sempre alguém que participou da derrota. Empates são resolvidos
 * pelo primeiro jogador a atingir o total e, por fim, pelo UUID para manter o
 * resultado determinístico.</p>
 */
@Service
public class WorldBossRewardService {
    private final WorldBossAttackRepository worldBossAttackRepository;
    private final WorldBossRewardRepository worldBossRewardRepository;
    private final AddItemUseCase addItemUseCase;

    /**
     * Concede o Baú da tentativa e, se este ataque derrotou o Boss, os dois
     * Baús especiais do encerramento.
     *
     * @param boss definição do Boss Mundial ativo
     * @param instance instância diária em processamento
     * @param attack ataque persistido que originou as recompensas
     * @param defeated indica se o ataque reduziu o HP a zero
     * @return Baús concedidos nesta operação, em ordem de tipo
     */
    @Transactional
    public List<WorldBossRewardResponse> grant(BossDefinitionEntity boss, WorldBossInstance instance, WorldBossAttack attack, boolean defeated) {
        List<WorldBossRewardResponse> rewards = new ArrayList<>();
        rewards.add(grantOne(boss, instance, attack, attack, WorldBossRewardType.ATTEMPT));
        if (defeated) {
            List<WorldBossAttack> attacks = new ArrayList<>(worldBossAttackRepository.findByWorldBossIdOrderByCreatedAtDesc(instance.getId()));
            if (attacks.stream().noneMatch(item -> item.getId().equals(attack.getId()))) {
                attacks.add(attack);
            }
            WorldBossAttack topDamageAttack = findTopDamageAttack(attacks);
            rewards.add(grantOne(boss, instance, attack, topDamageAttack, WorldBossRewardType.TOP_DAMAGE));
            rewards.add(grantOne(boss, instance, attack, attack, WorldBossRewardType.FINAL_BLOW));
        }
        return rewards;
    }

    /**
     * Retorna as recompensas originadas por um ataque específico.
     */
    @Transactional(readOnly = true)
    public List<WorldBossRewardResponse> findBySourceAttackId(UUID sourceAttackId) {
        return worldBossRewardRepository.findBySourceAttackIdOrderByRewardTypeAsc(sourceAttackId).stream().map(this::toResponse).toList();
    }

    /**
     * Retorna as recompensas oficiais do jogador nesta instância diária.
     */
    @Transactional(readOnly = true)
    public List<WorldBossRewardResponse> findPlayerRewards(UUID worldBossId, UUID playerId) {
        return worldBossRewardRepository.findByWorldBossIdAndRecipientPlayerIdOrderByCreatedAtAsc(worldBossId, playerId).stream().map(this::toResponse).toList();
    }

    private WorldBossRewardResponse grantOne(BossDefinitionEntity boss, WorldBossInstance instance, WorldBossAttack sourceAttack, WorldBossAttack recipientAttack, WorldBossRewardType rewardType) {
        String eventKey = "world-boss:" + instance.getId() + ":" + rewardType.getCode() + ":" + sourceAttack.getId();
        WorldBossReward existing = worldBossRewardRepository.findByEventKey(eventKey).orElse(null);
        if (existing != null) {
            return toResponse(existing);
        }
        ChestDefinitionEntity chest = resolveChest(boss, rewardType);
        addItemUseCase.addMaterial(recipientAttack.getDigimonId(), chest.getItemDefinition(), 1);
        WorldBossReward reward = WorldBossReward.builder().id(UUID.randomUUID()).worldBossId(instance.getId()).sourceAttackId(sourceAttack.getId()).recipientPlayerId(recipientAttack.getPlayerId()).recipientDigimonId(recipientAttack.getDigimonId()).chestDefinition(chest).rewardType(rewardType).eventKey(eventKey).createdAt(Instant.now()).build();
        return toResponse(worldBossRewardRepository.save(reward));
    }

    private ChestDefinitionEntity resolveChest(BossDefinitionEntity boss, WorldBossRewardType rewardType) {
        ChestDefinitionEntity chest = boss.chestForWorldReward(rewardType);
        String rewardLabel = switch (rewardType) {
            case ATTEMPT -> "tentativa";
            case TOP_DAMAGE -> "maior dano acumulado";
            case FINAL_BLOW -> "golpe final";
        };
        if (chest == null) {
            throw new ConflictException("Boss Mundial não possui Baú configurado para " + rewardLabel + ".");
        }
        if (!chest.isActive()) {
            throw new ConflictException("Baú de recompensa do Boss Mundial está inativo: " + chest.getCode());
        }
        if (chest.getLootTable() == null || !chest.getLootTable().isActive()) {
            throw new ConflictException("Loot Table do Baú de recompensa do Boss Mundial está inativa: " + chest.getCode());
        }
        return chest;
    }

    private WorldBossAttack findTopDamageAttack(List<WorldBossAttack> attacks) {
        Map<UUID, List<WorldBossAttack>> byPlayer = attacks.stream().collect(Collectors.groupingBy(WorldBossAttack::getPlayerId, LinkedHashMap::new, Collectors.toList()));
        return byPlayer.values().stream().map(playerAttacks -> new PlayerDamageSummary(playerAttacks.get(0).getPlayerId(), playerAttacks.stream().mapToLong(WorldBossAttack::getDamage).sum(), playerAttacks.stream().map(WorldBossAttack::getCreatedAt).min(Comparator.naturalOrder()).orElseThrow(), playerAttacks.stream().max(Comparator.comparing(WorldBossAttack::getCreatedAt).thenComparing(WorldBossAttack::getId)).orElseThrow())).sorted(Comparator.comparingLong(PlayerDamageSummary::totalDamage).reversed().thenComparing(PlayerDamageSummary::firstAttackAt).thenComparing(summary -> summary.playerId().toString())).map(PlayerDamageSummary::recipientAttack).findFirst().orElseThrow(() -> new ConflictException("Não foi possível identificar participante do Boss Mundial derrotado"));
    }

    private WorldBossRewardResponse toResponse(WorldBossReward reward) {
        return new WorldBossRewardResponse(reward.getRewardType().getCode(), reward.getChestDefinition().getCode(), reward.getChestDefinition().getName());
    }


    private record PlayerDamageSummary(UUID playerId, long totalDamage, Instant firstAttackAt, WorldBossAttack recipientAttack) {
    }

    public WorldBossRewardService(final WorldBossAttackRepository worldBossAttackRepository, final WorldBossRewardRepository worldBossRewardRepository, final AddItemUseCase addItemUseCase) {
        this.worldBossAttackRepository = worldBossAttackRepository;
        this.worldBossRewardRepository = worldBossRewardRepository;
        this.addItemUseCase = addItemUseCase;
    }
}
