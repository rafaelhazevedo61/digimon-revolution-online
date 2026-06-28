package com.dro.modules.equipment.domain;

import java.util.concurrent.ThreadLocalRandom;

public class EquipmentRarityRules {

    private static final int CHANCE_COMMON = 60;
    private static final int CHANCE_RARE = 25;
    private static final int CHANCE_EPIC = 12;
    private static final int CHANCE_LEGENDARY = 3;

    public static EquipmentRarity rollRarity() {
        int roll = ThreadLocalRandom.current().nextInt(1, 101);

        if (roll <= CHANCE_LEGENDARY) return EquipmentRarity.LEGENDARY;
        if (roll <= CHANCE_LEGENDARY + CHANCE_EPIC) return EquipmentRarity.EPIC;
        if (roll <= CHANCE_LEGENDARY + CHANCE_EPIC + CHANCE_RARE) return EquipmentRarity.RARE;
        return EquipmentRarity.COMMON;
    }
}
