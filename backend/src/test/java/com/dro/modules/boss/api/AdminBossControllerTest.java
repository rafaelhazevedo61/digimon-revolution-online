package com.dro.modules.boss.api;

import com.dro.modules.boss.api.dto.request.UpdateBossRequest;
import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.domain.BossDropEntity;
import com.dro.modules.boss.domain.BossType;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.boss.infra.BossDropRepository;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
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
                null, null, null, null, null, null, null, null, 42
        ));

        assertThat(response.getBody()).isSameAs(boss);
        assertThat(firstEquipment.getChance()).isEqualTo(42);
        assertThat(secondEquipment.getChance()).isEqualTo(42);
        assertThat(itemDrop.getChance()).isEqualTo(80);
        verify(bossDefinitionRepository).save(boss);
    }

    @Test
    void disablesCooldownWithoutChangingConfiguredMinutes() {
        BossDefinitionEntity boss = BossDefinitionEntity.builder()
                .id(8L)
                .bossType(BossType.WORLD)
                .cooldownMinutes(5)
                .cooldownEnabled(true)
                .build();

        when(bossDefinitionRepository.findWithDropsAndChestById(8L)).thenReturn(Optional.of(boss));
        when(bossDefinitionRepository.save(boss)).thenReturn(boss);

        controller.update(8L, new UpdateBossRequest(
                null, null, null, null, null, null, null, null, null,
                null, false, null, null, null, null, null, null, null
        ));

        assertThat(boss.isCooldownEnabled()).isFalse();
        assertThat(boss.getCooldownMinutes()).isEqualTo(5);
        verify(bossDefinitionRepository).save(boss);
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
