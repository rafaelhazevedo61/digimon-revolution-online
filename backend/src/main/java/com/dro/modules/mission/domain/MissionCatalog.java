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
                    ItemType.TRAINING_STONE
            ),

            new MissionDefinition(
                    "MISSION_2",
                    "Caça na Floresta",
                    "Derrote inimigos selvagens na floresta.",
                    5,
                    60,
                    ItemType.DIGITAMA_FIRE
            ),

            new MissionDefinition(
                    "MISSION_3",
                    "Desafio das Ruínas",
                    "Enfrente perigos nas ruínas antigas.",
                    10,
                    120,
                    ItemType.INCUBATOR_COMMON
            )
    );

    public static Optional<MissionDefinition> findById(String id) {
        return MISSIONS.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
    }
}
