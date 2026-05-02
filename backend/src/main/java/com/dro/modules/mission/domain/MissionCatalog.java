package com.dro.modules.mission.domain;

import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.loot.domain.LootItem;
import com.dro.modules.loot.domain.LootRarity;
import com.dro.modules.loot.domain.LootRarityChance;
import com.dro.modules.loot.domain.LootTable;

import java.util.List;
import java.util.Optional;

public class MissionCatalog {

    public static final List<MissionDefinition> MISSIONS = List.of(

            new MissionDefinition(
                    "MISSION_ADMIN",
                    "Missão de Teste",
                    "Missão administrativa para testes e debug.",
                    Area.NATIVE_FOREST,
                    Stage.BABY,
                    1,
                    999,
                    1,
                    5,
                    List.of(
                            new MissionReward(ItemType.TRAINING_STONE, 10),
                            new MissionReward(ItemType.DATA_CORE, 5),
                            new MissionReward(ItemType.FRAGMENT_CHAMPION, 3),
                            new MissionReward(ItemType.FRAGMENT_MEGA, 1)
                    ),
                    new LootTable(
                            List.of(
                                    new LootRarityChance(LootRarity.COMMON, 40),
                                    new LootRarityChance(LootRarity.RARE, 30),
                                    new LootRarityChance(LootRarity.EPIC, 20),
                                    new LootRarityChance(LootRarity.LEGENDARY, 10)
                            ),
                            List.of(
                                    new LootItem(LootRarity.COMMON, ItemType.TRAINING_STONE, 5),
                                    new LootItem(LootRarity.RARE, ItemType.DIGITAMA_FIRE, 1),
                                    new LootItem(LootRarity.EPIC, ItemType.INCUBATOR_EPIC, 1),
                                    new LootItem(LootRarity.LEGENDARY, ItemType.FRAGMENT_MEGA, 50)
                            )
                    )
            ),

            new MissionDefinition(
                    "MISSION_1",
                    "Patrulha na Floresta Nativa",
                    "Explore a floresta onde os Digimons recém-nascidos dão seus primeiros passos.",
                    Area.NATIVE_FOREST,
                    Stage.BABY,
                    1,
                    30,
                    5,
                    10,
                    List.of(
                            new MissionReward(ItemType.TRAINING_STONE, 1)
                    ),
                    new LootTable(
                            List.of(
                                    new LootRarityChance(LootRarity.COMMON, 70),
                                    new LootRarityChance(LootRarity.RARE, 20),
                                    new LootRarityChance(LootRarity.EPIC, 8),
                                    new LootRarityChance(LootRarity.LEGENDARY, 2)
                            ),
                            List.of(
                                    new LootItem(LootRarity.COMMON, ItemType.TRAINING_STONE, 1),
                                    new LootItem(LootRarity.RARE, ItemType.DATA_CORE, 1),
                                    new LootItem(LootRarity.EPIC, ItemType.DIGITAMA_FIRE, 1),
                                    new LootItem(LootRarity.LEGENDARY, ItemType.FRAGMENT_CHAMPION, 1)
                            )
                    )
            ),

            new MissionDefinition(
                    "MISSION_2",
                    "Caçada em Gear Savanna",
                    "Enfrente Digimons selvagens nas vastas planícies mecânicas de Gear Savanna.",
                    Area.GEAR_SAVANNA,
                    Stage.ROOKIE,
                    8,
                    60,
                    6,
                    30,
                    List.of(
                            new MissionReward(ItemType.TRAINING_STONE, 1),
                            new MissionReward(ItemType.DATA_CORE, 1)
                    ),
                    new LootTable(
                            List.of(
                                    new LootRarityChance(LootRarity.COMMON, 65),
                                    new LootRarityChance(LootRarity.RARE, 22),
                                    new LootRarityChance(LootRarity.EPIC, 10),
                                    new LootRarityChance(LootRarity.LEGENDARY, 3)
                            ),
                            List.of(
                                    new LootItem(LootRarity.COMMON, ItemType.DATA_CORE, 1),
                                    new LootItem(LootRarity.RARE, ItemType.DIGITAMA_WATER, 1),
                                    new LootItem(LootRarity.EPIC, ItemType.INCUBATOR_RARE, 1),
                                    new LootItem(LootRarity.LEGENDARY, ItemType.FRAGMENT_CHAMPION, 3)
                            )
                    )
            ),

            new MissionDefinition(
                    "MISSION_3",
                    "Investigação em Factorial Town",
                    "Infiltre-se na cidade-fábrica e colete dados das máquinas descontroladas.",
                    Area.FACTORIAL_TOWN,
                    Stage.ROOKIE,
                    15,
                    100,
                    7,
                    60,
                    List.of(
                            new MissionReward(ItemType.DATA_CORE, 2),
                            new MissionReward(ItemType.FRAGMENT_CHAMPION, 1)
                    ),
                    new LootTable(
                            List.of(
                                    new LootRarityChance(LootRarity.COMMON, 60),
                                    new LootRarityChance(LootRarity.RARE, 23),
                                    new LootRarityChance(LootRarity.EPIC, 12),
                                    new LootRarityChance(LootRarity.LEGENDARY, 5)
                            ),
                            List.of(
                                    new LootItem(LootRarity.COMMON, ItemType.TRAINING_STONE, 2),
                                    new LootItem(LootRarity.RARE, ItemType.DIGITAMA_NATURE, 1),
                                    new LootItem(LootRarity.EPIC, ItemType.INCUBATOR_EPIC, 1),
                                    new LootItem(LootRarity.LEGENDARY, ItemType.FRAGMENT_CHAMPION, 5)
                            )
                    )
            ),

            new MissionDefinition(
                    "MISSION_4",
                    "Expedição em Freezeland",
                    "Sobreviva ao frio extremo de Freezeland e derrote os Digimons de gelo.",
                    Area.FREEZELAND,
                    Stage.CHAMPION,
                    25,
                    180,
                    8,
                    120,
                    List.of(
                            new MissionReward(ItemType.DATA_CORE, 2),
                            new MissionReward(ItemType.FRAGMENT_CHAMPION, 2)
                    ),
                    new LootTable(
                            List.of(
                                    new LootRarityChance(LootRarity.COMMON, 55),
                                    new LootRarityChance(LootRarity.RARE, 25),
                                    new LootRarityChance(LootRarity.EPIC, 14),
                                    new LootRarityChance(LootRarity.LEGENDARY, 6)
                            ),
                            List.of(
                                    new LootItem(LootRarity.COMMON, ItemType.DATA_CORE, 2),
                                    new LootItem(LootRarity.RARE, ItemType.DIGITAMA_FIRE, 1),
                                    new LootItem(LootRarity.EPIC, ItemType.INCUBATOR_EPIC, 1),
                                    new LootItem(LootRarity.LEGENDARY, ItemType.FRAGMENT_ULTIMATE, 3)
                            )
                    )
            ),

            new MissionDefinition(
                    "MISSION_5",
                    "Travessia do Deserto do Server",
                    "Cruze o vasto deserto do Server Continent enfrentando Digimons poderosos.",
                    Area.SERVER_DESERT,
                    Stage.ULTIMATE,
                    40,
                    300,
                    9,
                    180,
                    List.of(
                            new MissionReward(ItemType.DATA_CORE, 3),
                            new MissionReward(ItemType.FRAGMENT_ULTIMATE, 2)
                    ),
                    new LootTable(
                            List.of(
                                    new LootRarityChance(LootRarity.COMMON, 50),
                                    new LootRarityChance(LootRarity.RARE, 27),
                                    new LootRarityChance(LootRarity.EPIC, 16),
                                    new LootRarityChance(LootRarity.LEGENDARY, 7)
                            ),
                            List.of(
                                    new LootItem(LootRarity.COMMON, ItemType.FRAGMENT_CHAMPION, 2),
                                    new LootItem(LootRarity.RARE, ItemType.DIGITAMA_WATER, 1),
                                    new LootItem(LootRarity.EPIC, ItemType.INCUBATOR_EPIC, 1),
                                    new LootItem(LootRarity.LEGENDARY, ItemType.FRAGMENT_ULTIMATE, 5)
                            )
                    )
            ),

            new MissionDefinition(
                    "MISSION_6",
                    "Ascensão à Infinity Mountain",
                    "Escale a lendária Infinity Mountain e enfrente os Digimons mais poderosos do Mundo Digital.",
                    Area.INFINITY_MOUNTAIN,
                    Stage.MEGA,
                    60,
                    500,
                    10,
                    300,
                    List.of(
                            new MissionReward(ItemType.FRAGMENT_ULTIMATE, 3),
                            new MissionReward(ItemType.INCUBATOR_RARE, 1)
                    ),
                    new LootTable(
                            List.of(
                                    new LootRarityChance(LootRarity.COMMON, 45),
                                    new LootRarityChance(LootRarity.RARE, 28),
                                    new LootRarityChance(LootRarity.EPIC, 18),
                                    new LootRarityChance(LootRarity.LEGENDARY, 9)
                            ),
                            List.of(
                                    new LootItem(LootRarity.COMMON, ItemType.FRAGMENT_CHAMPION, 3),
                                    new LootItem(LootRarity.RARE, ItemType.FRAGMENT_ULTIMATE, 2),
                                    new LootItem(LootRarity.EPIC, ItemType.INCUBATOR_EPIC, 1),
                                    new LootItem(LootRarity.LEGENDARY, ItemType.FRAGMENT_MEGA, 10)
                            )
                    )
            )
    );

    public static Optional<MissionDefinition> findById (String id) {
        return MISSIONS.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
    }
}
