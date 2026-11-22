package com.dro.modules.achievement.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.Rarity;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentSlot;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AchievementGameplayRecorderTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final AchievementGameplayRecorder recorder = new AchievementGameplayRecorder(jdbc);
    private final UUID player = UUID.randomUUID(), digimon = UUID.randomUUID();

    @Test void birthBindsTheValuesAtBirthInsteadOfTheMutableDigimon() {
        Digimon born = Digimon.builder().id(digimon).playerId(player).rarity(Rarity.RARE)
                .ivHp(100).ivAttack(50).ivDefense(60).build();
        recorder.birth(born);
        born.setRarity(Rarity.LEGENDARY);
        verify(jdbc).update(contains("INSERT INTO achievement_birth_snapshots"),
                eq(digimon), eq(player), eq("RARE"), eq(100), eq(50), eq(60), isNull());
    }

    @Test void completeRealSetRecordsBothFormationAndDistinctSet() {
        recorder.equipped(player, digimon, List.of(piece(EquipmentSlot.WEAPON, "BERSERKER"),
                piece(EquipmentSlot.ARMOR, "BERSERKER"), piece(EquipmentSlot.ACCESSORY, "BERSERKER")));
        verify(jdbc).update(anyString(), eq(player), eq("FULL_EQUIPPED"), eq(digimon.toString()));
        verify(jdbc).update(anyString(), eq(player), eq("SETS_ACTIVATED"), eq("BERSERKER"));
    }

    @Test void mixedEquipmentStillCompletesFormationButDoesNotActivateSet() {
        recorder.equipped(player, digimon, List.of(piece(EquipmentSlot.WEAPON, "BERSERKER"),
                piece(EquipmentSlot.ARMOR, "GUARDIAN"), piece(EquipmentSlot.ACCESSORY, "VITALITY")));
        verify(jdbc).update(anyString(), eq(player), eq("FULL_EQUIPPED"), eq(digimon.toString()));
        verify(jdbc, never()).update(anyString(), eq(player), eq("SETS_ACTIVATED"), anyString());
    }

    @Test void duplicateSlotsDoNotFormACompleteSet() {
        recorder.equipped(player, digimon, List.of(piece(EquipmentSlot.WEAPON, "BERSERKER"),
                piece(EquipmentSlot.WEAPON, "BERSERKER"), piece(EquipmentSlot.WEAPON, "BERSERKER")));
        verifyNoInteractions(jdbc);
    }

    @Test void equipmentOnAnotherDigimonDoesNotCount() {
        Equipment foreign = piece(EquipmentSlot.ACCESSORY, "BERSERKER");
        foreign.setDigimonId(UUID.randomUUID());
        recorder.equipped(player, digimon, List.of(piece(EquipmentSlot.WEAPON, "BERSERKER"),
                piece(EquipmentSlot.ARMOR, "BERSERKER"), foreign));
        verifyNoInteractions(jdbc);
    }

    @Test void actionKeysDescribeTheAttemptAndAscensionLevel() {
        UUID attempt = UUID.randomUUID();
        recorder.refinement(player, attempt);
        recorder.ascension(player, digimon, 2);
        verify(jdbc).update(anyString(), eq(player), eq("REFINEMENT_SUCCESSES"), eq(attempt.toString()));
        verify(jdbc).update(anyString(), eq(player), eq("ASCENSIONS_DONE"), eq(digimon + ":2"));
    }

    private Equipment piece(EquipmentSlot slot, String set) {
        return Equipment.builder().id(UUID.randomUUID()).playerId(player).digimonId(digimon)
                .slot(slot).setCode(set).equipped(true).build();
    }
}
