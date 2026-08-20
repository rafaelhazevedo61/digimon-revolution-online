package com.dro.modules.mission.domain;

import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.loot.domain.LootTable;
import lombok.Data;

import java.util.List;

/**
 * Definição de uma missão disponível para o jogador.
 *
 * <p>O loot legado continua exposto apenas para compatibilidade com dados
 * antigos. Missões migradas para o novo sistema usam {@code chestCode} para
 * entregar um Baú da Área.</p>
 */
@Data
public class MissionDefinition {

    private final String id;
    private final String name;
    private final String description;
    private final Area area;
    private final Stage requiredStage;
    private final int requiredLevel;
    private final int baseXp;
    private final int baseBits;
    private final int energyCost;
    private final int durationSeconds;
    private final List<MissionReward> fixedRewards;
    private final LootTable lootTable;
    private final String chestCode;
}
