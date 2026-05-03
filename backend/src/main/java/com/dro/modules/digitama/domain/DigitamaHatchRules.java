package com.dro.modules.digitama.domain;

import com.dro.modules.inventory.domain.ItemType;

import java.util.*;

public class DigitamaHatchRules {

    private static final Map<ItemType, DigitamaType> ITEM_TO_DIGITAMA = Map.of(
            ItemType.DIGITAMA_STARTER, DigitamaType.STARTER,
            ItemType.DIGITAMA_FIRE, DigitamaType.FIRE,
            ItemType.DIGITAMA_WATER, DigitamaType.WATER,
            ItemType.DIGITAMA_NATURE, DigitamaType.NATURE
    );

    private static final Map<DigitamaType, List<String>> HATCH_TABLE = Map.of(
            DigitamaType.STARTER, List.of("Botamon", "Pichimon", "Pabumon", "Punimon", "Poyomon", "Yuramon"),
            DigitamaType.FIRE, List.of("Botamon", "Punimon"),
            DigitamaType.WATER, List.of("Pichimon", "Poyomon"),
            DigitamaType.NATURE, List.of("Pabumon", "Yuramon")
    );

    private static final Random random = new Random();

    private DigitamaHatchRules() {
    }

    public static List<String> getPossibleBabies(DigitamaType type) {
        List<String> babies = HATCH_TABLE.get(type);
        if (babies == null) {
            throw new IllegalArgumentException("Unknown digitama type: " + type);
        }
        return babies;
    }

    public static String rollBabyName(DigitamaType type) {
        List<String> babies = getPossibleBabies(type);
        return babies.get(random.nextInt(babies.size()));
    }

    public static DigitamaType toDigitamaType(ItemType itemType) {
        DigitamaType type = ITEM_TO_DIGITAMA.get(itemType);
        if (type == null) {
            throw new IllegalArgumentException("Not a digitama item: " + itemType);
        }
        return type;
    }
}
