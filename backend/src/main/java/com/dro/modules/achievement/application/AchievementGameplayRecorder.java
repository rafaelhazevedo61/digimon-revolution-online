package com.dro.modules.achievement.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.domain.EquipmentSlot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/** Evidências imutáveis de ações reais, gravadas na mesma transação da ação. */
@Service
@Transactional(propagation = Propagation.MANDATORY)
public class AchievementGameplayRecorder {
    private final JdbcTemplate jdbc;

    public AchievementGameplayRecorder(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Chamado somente pelos dois fluxos de nascimento; nunca por reroll, compra ou rebirth. */
    public void birth(Digimon digimon) {
        jdbc.update("""
                INSERT INTO achievement_birth_snapshots
                    (digimon_id, player_id, rarity, iv_hp, iv_attack, iv_defense, trait)
                VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (digimon_id) DO NOTHING
                """, digimon.getId(), digimon.getPlayerId(), digimon.getRarity().name(),
                digimon.getIvHp(), digimon.getIvAttack(), digimon.getIvDefense(),
                digimon.getTrait() == null ? null : digimon.getTrait().name());
    }

    public void refinement(UUID playerId, UUID attemptId) {
        record(playerId, "REFINEMENT_SUCCESSES", attemptId.toString());
    }

    public void ascension(UUID playerId, UUID equipmentId, int level) {
        record(playerId, "ASCENSIONS_DONE", equipmentId + ":" + level);
    }

    public void equipped(UUID playerId, UUID digimonId, List<Equipment> pieces) {
        List<Equipment> equipped = pieces.stream()
                .filter(e -> playerId.equals(e.getPlayerId()) && digimonId.equals(e.getDigimonId()) && e.isEquipped())
                .toList();
        var slots = EnumSet.noneOf(EquipmentSlot.class);
        equipped.forEach(e -> slots.add(e.getSlot()));
        boolean complete = slots.containsAll(EnumSet.of(EquipmentSlot.WEAPON, EquipmentSlot.ARMOR, EquipmentSlot.ACCESSORY));
        if (complete) {
            record(playerId, "FULL_EQUIPPED", digimonId.toString());
        }
        var bonus = EquipmentRules.getSetBonusInfo(equipped);
        if (complete && bonus.setCode() != null && bonus.pieceCount() >= 3) {
            // O código do conjunto é a chave: repetir o mesmo set não conta como um set novo.
            record(playerId, "SETS_ACTIVATED", bonus.setCode());
        }
    }

    private void record(UUID playerId, String metric, String eventKey) {
        jdbc.update("""
                INSERT INTO achievement_gameplay_events(player_id, metric, event_key)
                VALUES (?, ?, ?) ON CONFLICT DO NOTHING
                """, playerId, metric, eventKey);
    }
}
