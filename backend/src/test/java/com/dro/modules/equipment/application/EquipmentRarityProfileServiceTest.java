package com.dro.modules.equipment.application;

import com.dro.modules.equipment.api.dto.request.UpdateEquipmentRarityProfileRequest;
import com.dro.modules.equipment.domain.EquipmentRarityProfileEntity;
import com.dro.modules.equipment.infra.EquipmentRarityProfileRepository;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipmentRarityProfileServiceTest {

    @Mock
    private EquipmentRarityProfileRepository repository;

    @Mock
    private TransactionAuditPublisher transactionAuditPublisher;

    private EquipmentRarityProfileService service;

    @BeforeEach
    void setUp() {
        service = new EquipmentRarityProfileService(repository, transactionAuditPublisher);
    }

    @Test
    void listsOnlyBossProfiles() {
        EquipmentRarityProfileEntity boss = profile("BOSS_NORMAL", 65, 22, 10, 3);
        EquipmentRarityProfileEntity shop = profile("SHOP", 100, 0, 0, 0);
        when(repository.findAllByOrderByProfileKeyAsc()).thenReturn(List.of(boss, shop));

        var result = service.listBossProfiles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).profileKey()).isEqualTo("BOSS_NORMAL");
    }

    @Test
    void rejectsProfileWhenPercentagesDoNotSumToOneHundred() {
        var request = new UpdateEquipmentRarityProfileRequest(60, 20, 10, 5);

        assertThatThrownBy(() -> service.updateBossProfile("BOSS_NORMAL", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Soma atual: 95%");
    }

    @Test
    void updatesProfileAndAuditsConfiguration() {
        EquipmentRarityProfileEntity profile = profile("BOSS_WEEKLY", 40, 30, 20, 10);
        when(repository.findByProfileKey("BOSS_WEEKLY")).thenReturn(Optional.of(profile));
        when(repository.save(profile)).thenReturn(profile);

        var result = service.updateBossProfile(
                "boss_weekly",
                new UpdateEquipmentRarityProfileRequest(35, 30, 20, 15)
        );

        assertThat(result.commonPercent()).isEqualTo(35);
        assertThat(result.rarePercent()).isEqualTo(30);
        assertThat(result.epicPercent()).isEqualTo(20);
        assertThat(result.legendaryPercent()).isEqualTo(15);
        verify(transactionAuditPublisher).success(
                eq("equipment-rarity-profile:BOSS_WEEKLY"),
                eq("ADMIN_EQUIPMENT_RARITY_PROFILE_UPDATED"),
                eq("EquipmentRarityProfile"),
                eq("BOSS_WEEKLY"),
                anyMap()
        );
    }

    @Test
    void resolvesPersistedProfileForCombat() {
        when(repository.findByProfileKey("BOSS_MONTHLY"))
                .thenReturn(Optional.of(profile("BOSS_MONTHLY", 10, 20, 30, 40)));

        var result = service.resolve("BOSS_MONTHLY");

        assertThat(result.common()).isEqualTo(10);
        assertThat(result.rare()).isEqualTo(20);
        assertThat(result.epic()).isEqualTo(30);
        assertThat(result.legendary()).isEqualTo(40);
    }

    private EquipmentRarityProfileEntity profile(
            String key,
            int common,
            int rare,
            int epic,
            int legendary
    ) {
        return EquipmentRarityProfileEntity.builder()
                .profileKey(key)
                .displayName(key)
                .commonPercent(common)
                .rarePercent(rare)
                .epicPercent(epic)
                .legendaryPercent(legendary)
                .updatedBy("SYSTEM")
                .build();
    }
}
