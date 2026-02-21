package com.dro.modules.mission.domain;

import com.dro.modules.inventory.domain.ItemType;

import java.util.List;
import java.util.Optional;

public class MissionCatalog {

    public static final List<MissionDefinition> MISSIONS = List.of(

            new MissionDefinition(
                    "MISSION_1",
                    "Treinamento Inicial",
                    "Uma missão básica para novos Digimons.",
                    1,
                    30,
                    List.of(
                            new MissionReward(ItemType.TRAINING_STONE, 1)
                    )
            ),

            new MissionDefinition(
                    "MISSION_2",
                    "Caça na Floresta",
                    "Derrote inimigos selvagens na floresta.",
                    5,
                    60,
                    List.of(
                            new MissionReward(ItemType.TRAINING_STONE, 1)
                    )
            ),

            new MissionDefinition(
                    "MISSION_3",
                    "Desafio das Ruínas",
                    "Enfrente perigos nas ruínas antigas.",
                    10,
                    120,
                    List.of(
                            new MissionReward(ItemType.TRAINING_STONE, 1)
                    )
            )
    );

    public static Optional<MissionDefinition> findById(String id) {
        return MISSIONS.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
    }
}
