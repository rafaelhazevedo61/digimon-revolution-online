package com.dro.modules.boss.api;

import com.dro.modules.boss.api.dto.request.CreateBossDropRequest;
import com.dro.modules.boss.api.dto.request.CreateBossRequest;
import com.dro.modules.boss.api.dto.request.UpdateBossRequest;
import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.domain.BossDropEntity;
import com.dro.modules.boss.domain.BossType;
import com.dro.modules.boss.api.dto.response.BossChestOptionResponse;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.boss.infra.BossDropRepository;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.infra.ChestDefinitionRepository;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Componente da camada de controller da API do módulo de Boss Mundial.
 */
@RestController
@RequestMapping("/admin/bosses")
public class AdminBossController {
    private final BossDefinitionRepository bossDefinitionRepository;
    private final BossDropRepository bossDropRepository;
    private final ChestDefinitionRepository chestDefinitionRepository;

    @GetMapping("/chest-options")
    public ResponseEntity<List<BossChestOptionResponse>> chestOptions() {
        return ResponseEntity.ok(chestDefinitionRepository.findByActiveTrueAndCodeStartingWithOrderByNameAsc("CHEST_BOSS_").stream().map(chest -> new BossChestOptionResponse(chest.getCode(), chest.getName(), chest.getLootTable().getCode(), chest.getLootTable().getName(), chest.isActive(), chest.isTradable())).toList());
    }

    @GetMapping
    public ResponseEntity<List<BossDefinitionEntity>> listAll() {
        return ResponseEntity.ok(bossDefinitionRepository.findAllByOrderByIdAsc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BossDefinitionEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bossDefinitionRepository.findWithDropsAndChestById(id).orElseThrow(() -> new NotFoundException("Boss not found")));
    }

    @PostMapping
    public ResponseEntity<BossDefinitionEntity> create(@RequestBody @Valid CreateBossRequest request) {
        BossType bossType = BossType.valueOf(request.bossType());
        ChestDefinitionEntity rewardChest = requiresRewardChest(bossType) ? resolveActiveChest(request.chestCode()) : null;
        ChestDefinitionEntity worldAttemptChest = requiresWorldRewardChests(bossType) ? resolveActiveChest(request.worldAttemptChestCode()) : null;
        ChestDefinitionEntity worldTopDamageChest = requiresWorldRewardChests(bossType) ? resolveActiveChest(request.worldTopDamageChestCode()) : null;
        ChestDefinitionEntity worldFinalBlowChest = requiresWorldRewardChests(bossType) ? resolveActiveChest(request.worldFinalBlowChestCode()) : null;
        BossDefinitionEntity boss = BossDefinitionEntity.builder().code(request.code()).name(request.name()).bossType(bossType).requiredStage(Stage.valueOf(request.requiredStage())).requiredLevel(request.requiredLevel()).requiredRebirths(request.requiredRebirths()).hp(request.hp()).atk(request.atk()).def(request.def()).energyCost(request.energyCost()).cooldownMinutes(request.cooldownMinutes()).cooldownEnabled(request.cooldownEnabled() == null || request.cooldownEnabled()).baseXpReward(request.baseXpReward()).baseBitsReward(request.baseBitsReward()).defeatXpPercent(request.defeatXpPercent() != null ? request.defeatXpPercent() : 10).imageUrl(request.imageUrl()).chestDefinition(rewardChest).worldAttemptChestDefinition(worldAttemptChest).worldTopDamageChestDefinition(worldTopDamageChest).worldFinalBlowChestDefinition(worldFinalBlowChest).active(true).build();
        return ResponseEntity.ok(bossDefinitionRepository.save(boss));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BossDefinitionEntity> update(@PathVariable Long id, @RequestBody @Valid UpdateBossRequest request) {
        BossDefinitionEntity boss = bossDefinitionRepository.findWithDropsAndChestById(id).orElseThrow(() -> new NotFoundException("Boss not found"));
        BossType resultingType = request.bossType() != null ? BossType.valueOf(request.bossType()) : boss.getBossType();
        if (request.name() != null) boss.setName(request.name());
        if (request.bossType() != null) boss.setBossType(resultingType);
        if (request.requiredStage() != null) boss.setRequiredStage(Stage.valueOf(request.requiredStage()));
        if (request.requiredLevel() != null) boss.setRequiredLevel(request.requiredLevel());
        if (request.requiredRebirths() != null) boss.setRequiredRebirths(request.requiredRebirths());
        if (request.hp() != null) boss.setHp(request.hp());
        if (request.atk() != null) boss.setAtk(request.atk());
        if (request.def() != null) boss.setDef(request.def());
        if (request.energyCost() != null) boss.setEnergyCost(request.energyCost());
        if (request.cooldownMinutes() != null) boss.setCooldownMinutes(request.cooldownMinutes());
        if (request.cooldownEnabled() != null) boss.setCooldownEnabled(request.cooldownEnabled());
        if (request.baseXpReward() != null) boss.setBaseXpReward(request.baseXpReward());
        if (request.baseBitsReward() != null) boss.setBaseBitsReward(request.baseBitsReward());
        if (request.defeatXpPercent() != null) boss.setDefeatXpPercent(request.defeatXpPercent());
        if (request.imageUrl() != null) boss.setImageUrl(request.imageUrl());
        if (request.active() != null) boss.setActive(request.active());
        if (requiresRewardChest(resultingType)) {
            if (request.chestCode() != null && !request.chestCode().isBlank()) {
                boss.setChestDefinition(resolveActiveChest(request.chestCode()));
            } else if (boss.getChestDefinition() == null) {
                throw new ConflictException("Boss normal ou periódico precisa de um Baú de recompensa ativo.");
            }
        } else if (request.chestCode() != null && !request.chestCode().isBlank()) {
            boss.setChestDefinition(resolveActiveChest(request.chestCode()));
        }
        if (requiresWorldRewardChests(resultingType)) {
            if (request.worldAttemptChestCode() != null && !request.worldAttemptChestCode().isBlank()) {
                boss.setWorldAttemptChestDefinition(resolveActiveChest(request.worldAttemptChestCode()));
            }
            if (request.worldTopDamageChestCode() != null && !request.worldTopDamageChestCode().isBlank()) {
                boss.setWorldTopDamageChestDefinition(resolveActiveChest(request.worldTopDamageChestCode()));
            }
            if (request.worldFinalBlowChestCode() != null && !request.worldFinalBlowChestCode().isBlank()) {
                boss.setWorldFinalBlowChestDefinition(resolveActiveChest(request.worldFinalBlowChestCode()));
            }
            ensureWorldRewardChestsConfigured(boss);
        }
        if (request.equipmentChance() != null) {
            updateEquipmentPoolChance(boss, request.equipmentChance());
        }
        return ResponseEntity.ok(bossDefinitionRepository.save(boss));
    }

    /**
     * Atualiza a chance única da pool em todos os templates de equipamento do Boss.
     *
     * <p>O combate faz um único roll usando a chance do primeiro drop da pool e,
     * após o sucesso, escolhe uniformemente entre os templates. Por isso, a edição
     * administrativa mantém o mesmo percentual em todos os drops de equipamento.</p>
     *
     * @param boss Boss que terá a pool atualizada
     * @param chance percentual entre zero e cem
     */
    private void updateEquipmentPoolChance(BossDefinitionEntity boss, int chance) {
        List<BossDropEntity> equipmentDrops = boss.getDrops() == null ? List.of() : boss.getDrops().stream().filter(drop -> "EQUIPMENT".equalsIgnoreCase(drop.getDropType())).toList();
        if (equipmentDrops.isEmpty()) {
            throw new ConflictException("Boss não possui drops de equipamento para configurar a chance da pool.");
        }
        equipmentDrops.forEach(drop -> drop.setChance(chance));
    }

    private boolean requiresRewardChest(BossType bossType) {
        return bossType == BossType.NORMAL || bossType == BossType.DAILY || bossType == BossType.WEEKLY || bossType == BossType.MONTHLY;
    }

    private boolean requiresWorldRewardChests(BossType bossType) {
        return bossType == BossType.WORLD;
    }

    private void ensureWorldRewardChestsConfigured(BossDefinitionEntity boss) {
        if (boss.getWorldAttemptChestDefinition() == null || boss.getWorldTopDamageChestDefinition() == null || boss.getWorldFinalBlowChestDefinition() == null) {
            throw new ConflictException("Boss Mundial precisa dos Baús ativos de tentativa, maior dano e golpe final.");
        }
    }

    private ChestDefinitionEntity resolveActiveChest(String code) {
        String normalizedCode = code == null ? "" : code.trim();
        ChestDefinitionEntity chest = chestDefinitionRepository.findWithCatalogByCode(normalizedCode).orElseThrow(() -> new ConflictException("Baú de recompensa não encontrado: " + normalizedCode));
        if (!chest.isActive()) {
            throw new ConflictException("Baú de recompensa está inativo: " + normalizedCode);
        }
        if (!chest.getCode().startsWith("CHEST_BOSS_")) {
            throw new ConflictException("O Baú selecionado não pertence ao catálogo de Baús de Boss: " + normalizedCode);
        }
        if (chest.getLootTable() == null || !chest.getLootTable().isActive()) {
            throw new ConflictException("A Loot Table do Baú de recompensa está inativa: " + normalizedCode);
        }
        return chest;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        bossDefinitionRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Boss deleted"));
    }

    @PostMapping("/{bossId}/drops")
    public ResponseEntity<BossDropEntity> addDrop(@PathVariable Long bossId, @RequestBody @Valid CreateBossDropRequest request) {
        BossDefinitionEntity boss = bossDefinitionRepository.findById(bossId).orElseThrow(() -> new NotFoundException("Boss not found"));
        if (requiresRewardChest(boss.getBossType()) && "ITEM".equalsIgnoreCase(request.dropType())) {
            throw new ConflictException("Bosses normais e periódicos não aceitam novos drops diretos de itens; configure a Loot Table do Baú.");
        }
        BossDropEntity drop = BossDropEntity.builder().boss(boss).dropType(request.dropType()).itemCode(request.itemCode()).templateName(request.templateName()).equipmentRarity(request.equipmentRarity()).chance(request.chance()).minQuantity(request.minQuantity()).maxQuantity(request.maxQuantity()).build();
        return ResponseEntity.ok(bossDropRepository.save(drop));
    }

    @DeleteMapping("/drops/{dropId}")
    public ResponseEntity<Map<String, String>> deleteDrop(@PathVariable Long dropId) {
        bossDropRepository.deleteById(dropId);
        return ResponseEntity.ok(Map.of("message", "Drop deleted"));
    }

    public AdminBossController(final BossDefinitionRepository bossDefinitionRepository, final BossDropRepository bossDropRepository, final ChestDefinitionRepository chestDefinitionRepository) {
        this.bossDefinitionRepository = bossDefinitionRepository;
        this.bossDropRepository = bossDropRepository;
        this.chestDefinitionRepository = chestDefinitionRepository;
    }
}
