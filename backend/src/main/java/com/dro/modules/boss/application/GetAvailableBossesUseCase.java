package com.dro.modules.boss.application;

import com.dro.modules.boss.api.dto.response.BossDefinitionResponse;
import com.dro.modules.boss.api.dto.response.BossDropResponse;
import com.dro.modules.boss.domain.BossCombatRules;
import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.infra.BossAttemptRepository;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.dro.modules.boss.domain.BossType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAvailableBossesUseCase {

    private final BossDefinitionRepository bossDefinitionRepository;
    private final BossAttemptRepository bossAttemptRepository;
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final EquipmentRepository equipmentRepository;

    public List<BossDefinitionResponse> execute(String token) {

        UUID playerId = TokenExtractor.extractPlayerId(token);
        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active digimon not found"));

        List<Equipment> equippedItems = equipmentRepository.findByDigimonId(digimon.getId())
                .stream().filter(Equipment::isEquipped).toList();
        int totalHp = digimon.getHp() + EquipmentRules.totalBonusHp(equippedItems);
        int totalAtk = digimon.getAttack() + EquipmentRules.totalBonusAttack(equippedItems);
        int totalDef = digimon.getDefense() + EquipmentRules.totalBonusDefense(equippedItems);
        double digimonPower = BossCombatRules.calculatePower(totalHp, totalAtk, totalDef);

        List<BossDefinitionEntity> bosses = bossDefinitionRepository.findAllActive().stream()
                .filter(boss -> boss.getBossType() != BossType.CLAN && boss.getBossType() != BossType.WORLD)
                .toList();

        Set<Long> todaysDailyBossIds = getTodaysDailyBossIds(bosses);

        return bosses.stream()
                .filter(boss -> boss.getBossType() != BossType.DAILY || todaysDailyBossIds.contains(boss.getId()))
                .map(boss -> {
            boolean meetsRequirements = digimon.getStage().ordinal() >= boss.getRequiredStage().ordinal()
                    && digimon.getLevel() >= boss.getRequiredLevel()
                    && digimon.getRebirthCount() >= boss.getRequiredRebirths();

            Long cooldownRemaining = calculateCooldownRemaining(playerId, boss);
            boolean available = meetsRequirements && (cooldownRemaining == null || cooldownRemaining <= 0);

            double bossPower = BossCombatRules.calculatePower(boss.getHp(), boss.getAtk(), boss.getDef());
            Integer winChance = meetsRequirements ? BossCombatRules.calculateWinChance(digimonPower, bossPower) : null;

            List<BossDropResponse> drops = boss.getDrops() != null
                    ? boss.getDrops().stream().map(d -> new BossDropResponse(
                    d.getDropType(), d.getItemCode(), d.getTemplateName(),
                    d.getEquipmentRarity(), d.getChance(), d.getMinQuantity(), d.getMaxQuantity()
            )).toList()
                    : List.of();

            return new BossDefinitionResponse(
                    boss.getId(),
                    boss.getCode(),
                    boss.getName(),
                    boss.getBossType().name(),
                    boss.getRequiredStage().name(),
                    boss.getRequiredLevel(),
                    boss.getRequiredRebirths(),
                    boss.getHp(),
                    boss.getAtk(),
                    boss.getDef(),
                    boss.getEnergyCost(),
                    boss.getCooldownMinutes(),
                    boss.getBaseXpReward(),
                    boss.getBaseBitsReward(),
                    boss.getImageUrl(),
                    available,
                    cooldownRemaining != null && cooldownRemaining > 0 ? cooldownRemaining : null,
                    winChance,
                    drops
            );
        }).toList();
    }

    private Set<Long> getTodaysDailyBossIds(List<BossDefinitionEntity> allBosses) {
        long dayIndex = LocalDate.now(ZoneOffset.UTC).toEpochDay();

        Map<String, List<BossDefinitionEntity>> dailyByStage = allBosses.stream()
                .filter(b -> b.getBossType() == BossType.DAILY)
                .sorted(Comparator.comparingLong(BossDefinitionEntity::getId))
                .collect(Collectors.groupingBy(b -> b.getRequiredStage().name(), LinkedHashMap::new, Collectors.toList()));

        Set<Long> result = new HashSet<>();
        for (List<BossDefinitionEntity> group : dailyByStage.values()) {
            if (group.isEmpty()) continue;
            int todayIndex = (int) (dayIndex % group.size());
            result.add(group.get(todayIndex).getId());
        }
        return result;
    }

    private Long calculateCooldownRemaining(UUID playerId, BossDefinitionEntity boss) {
        var lastAttempt = bossAttemptRepository
                .findFirstByPlayerIdAndBossIdOrderByCreatedAtDesc(playerId, boss.getId());

        if (lastAttempt.isEmpty()) return null;

        Instant lastTime = lastAttempt.get().getCreatedAt();
        Instant cooldownEnd = lastTime.plus(boss.getCooldownMinutes(), ChronoUnit.MINUTES);
        Instant now = Instant.now();

        if (now.isBefore(cooldownEnd)) {
            return now.until(cooldownEnd, ChronoUnit.SECONDS);
        }
        return null;
    }
}
