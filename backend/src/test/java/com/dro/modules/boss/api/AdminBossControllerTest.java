package com.dro.modules.boss.api;

import com.dro.modules.boss.api.dto.request.UpdateBossRequest;
import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.domain.BossDropEntity;
import com.dro.modules.boss.domain.BossType;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.boss.infra.BossDropRepository;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.domain.LootTableEntity;
import com.dro.modules.loot.infra.ChestDefinitionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBossControllerTest {

    @Mock
    private BossDefinitionRepository bossDefinitionRepository;

    @Mock
    private BossDropRepository bossDropRepository;

    @Mock
    private ChestDefinitionRepository chestDefinitionRepository;

    @InjectMocks
    private AdminBossController controller;

    @Test
    void updatesEquipmentChanceForEveryTemplateInThePool() {
        BossDefinitionEntity boss = BossDefinitionEntity.builder()
                .id(7L)
                .bossType(BossType.DAILY)
                .chestDefinition(ChestDefinitionEntity.builder().code("CHEST_BOSS_DAILY_TEST").build())
                .build();
        BossDropEntity firstEquipment = equipmentDrop(boss, "Garra Berserker T3", 25);
        BossDropEntity secondEquipment = equipmentDrop(boss, "Asa Anjo T3", 25);
        BossDropEntity itemDrop = BossDropEntity.builder()
                .boss(boss)
                .dropType("ITEM")
                .itemCode("POTION_SMALL")
                .chance(80)
                .minQuantity(1)
                .maxQuantity(1)
                .build();
        boss.setDrops(new ArrayList<>(List.of(firstEquipment, secondEquipment, itemDrop)));

        when(bossDefinitionRepository.findWithDropsAndChestById(7L)).thenReturn(Optional.of(boss));
        when(bossDefinitionRepository.save(boss)).thenReturn(boss);

        var response = controller.update(7L, new UpdateBossRequest(
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, 42,
                null, null, null
        ));

        assertThat(response.getBody()).isSameAs(boss);
        assertThat(firstEquipment.getChance()).isEqualTo(42);
        assertThat(secondEquipment.getChance()).isEqualTo(42);
        assertThat(itemDrop.getChance()).isEqualTo(80);
        verify(bossDefinitionRepository).save(boss);
    }

    @Test
    void ignoresIndividualCooldownToggleForWorldBoss() {
        BossDefinitionEntity boss = BossDefinitionEntity.builder()
                .id(8L)
                .bossType(BossType.WORLD)
                .cooldownMinutes(5)
                .cooldownEnabled(true)
                .worldAttemptChestDefinition(ChestDefinitionEntity.builder().code("CHEST_WORLD_ATTEMPT").build())
                .worldTopDamageChestDefinition(ChestDefinitionEntity.builder().code("CHEST_WORLD_TOP_DAMAGE").build())
                .worldFinalBlowChestDefinition(ChestDefinitionEntity.builder().code("CHEST_WORLD_FINAL_BLOW").build())
                .build();

        when(bossDefinitionRepository.findWithDropsAndChestById(8L)).thenReturn(Optional.of(boss));
        when(bossDefinitionRepository.save(boss)).thenReturn(boss);

        controller.update(8L, new UpdateBossRequest(
                null, null, null, null, null, null, null, null, null,
                null, false, null, null, null, null, null, null, null,
                null, null, null
        ));

        assertThat(boss.isCooldownEnabled()).isTrue();
        assertThat(boss.getCooldownMinutes()).isEqualTo(5);
        verify(bossDefinitionRepository).save(boss);
    }

    @Test
    void updatesWorldBossRewardChestsIndependently() {
        BossDefinitionEntity boss = BossDefinitionEntity.builder()
                .id(9L)
                .bossType(BossType.WORLD)
                .worldAttemptChestDefinition(ChestDefinitionEntity.builder().code("CHEST_WORLD_ATTEMPT_OLD").build())
                .worldTopDamageChestDefinition(ChestDefinitionEntity.builder().code("CHEST_WORLD_TOP_DAMAGE_OLD").build())
                .worldFinalBlowChestDefinition(ChestDefinitionEntity.builder().code("CHEST_WORLD_FINAL_BLOW_OLD").build())
                .build();
        ChestDefinitionEntity attempt = activeChest("CHEST_BOSS_WORLD_TEST_ATTEMPT");
        ChestDefinitionEntity topDamage = activeChest("CHEST_BOSS_WORLD_TEST_TOP_DAMAGE");
        ChestDefinitionEntity finalBlow = activeChest("CHEST_BOSS_WORLD_TEST_FINAL_BLOW");

        when(bossDefinitionRepository.findWithDropsAndChestById(9L)).thenReturn(Optional.of(boss));
        when(chestDefinitionRepository.findWithCatalogByCode(attempt.getCode())).thenReturn(Optional.of(attempt));
        when(chestDefinitionRepository.findWithCatalogByCode(topDamage.getCode())).thenReturn(Optional.of(topDamage));
        when(chestDefinitionRepository.findWithCatalogByCode(finalBlow.getCode())).thenReturn(Optional.of(finalBlow));
        when(bossDefinitionRepository.save(boss)).thenReturn(boss);

        controller.update(9L, new UpdateBossRequest(
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                attempt.getCode(), topDamage.getCode(), finalBlow.getCode()
        ));

        assertThat(boss.getWorldAttemptChestDefinition()).isSameAs(attempt);
        assertThat(boss.getWorldTopDamageChestDefinition()).isSameAs(topDamage);
        assertThat(boss.getWorldFinalBlowChestDefinition()).isSameAs(finalBlow);
        verify(bossDefinitionRepository).save(boss);
    }

    private ChestDefinitionEntity activeChest(String code) {
        ChestDefinitionEntity chest = mock(ChestDefinitionEntity.class);
        LootTableEntity lootTable = mock(LootTableEntity.class);
        when(chest.getCode()).thenReturn(code);
        when(chest.isActive()).thenReturn(true);
        when(chest.getLootTable()).thenReturn(lootTable);
        when(lootTable.isActive()).thenReturn(true);
        return chest;
    }

    private BossDropEntity equipmentDrop(BossDefinitionEntity boss, String templateName, int chance) {
        return BossDropEntity.builder()
                .boss(boss)
                .dropType("EQUIPMENT")
                .templateName(templateName)
                .chance(chance)
                .minQuantity(1)
                .maxQuantity(1)
                .build();
    }
}
