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
                    Area.DIGITAL_FOREST,
                    1,
                    30,
                    5,
                    10,
                    List.of(
                            new MissionReward(ItemType.TRAINING_STONE, 1),
                            new MissionReward(ItemType.DIGITAMA_FIRE, 1),
                            new MissionReward(ItemType.INCUBATOR_EPIC, 1),
                            new MissionReward(ItemType.FRAGMENT_CHAMPION, 50),
                            new MissionReward(ItemType.FRAGMENT_ULTIMATE, 50),
                            new MissionReward(ItemType.FRAGMENT_MEGA, 50)
                    )
            ),

            new MissionDefinition(
                    "MISSION_2",
                    "Caça na Floresta",
                    "Derrote inimigos selvagens na floresta.",
                    Area.DIGITAL_FOREST,
                    5,
                    60,
                    6,
                    10,
                    List.of(
                            new MissionReward(ItemType.TRAINING_STONE, 1)
                    )
            ),

            new MissionDefinition(
                    "MISSION_3",
                    "Desafio das Ruínas",
                    "Enfrente perigos nas ruínas antigas.",
                    Area.DIGITAL_FOREST,
                    10,
                    120,
                    7,
                    10,
                    List.of(
                            new MissionReward(ItemType.TRAINING_STONE, 1)
                    )
            ),

            new MissionDefinition(
                    "MISSION_4",
                    "Missao 4 - name",
                    "Missao 4 - description",
                    Area.OCEAN_DEPTHS,
                    10,
                    120,
                    8,
                    10,
                    List.of(
                            new MissionReward(ItemType.TRAINING_STONE, 1)
                    )
            ),

            new MissionDefinition(
                    "MISSION_5",
                    "Missao 5 - name",
                    "Missao 5 - description",
                    Area.ANCIENT_RUINS,
                    10,
                    120,
                    9,
                    10,
                    List.of(
                            new MissionReward(ItemType.TRAINING_STONE, 1)
                    )
            ),

            new MissionDefinition(
                    "MISSION_6",
                    "Missao 6 - name",
                    "Missao 6 - description",
                    Area.VOLCANIC_ZONE,
                    10,
                    120,
                    10,
                    10,
                    List.of(
                            new MissionReward(ItemType.TRAINING_STONE, 1)
                    )
            )
    );

    public static Optional<MissionDefinition> findById (String id) {
        return MISSIONS.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
    }
}
