package com.dro.modules.digitama.domain;

import com.dro.modules.digitama.domain.enums.DigitamaType;
import com.dro.modules.inventory.domain.ItemType;

import java.util.*;

/**
 * Componente da camada de conjunto de regras de domínio do módulo de Digitama.
 */
public class DigitamaHatchRules {

    private static final Map<ItemType, DigitamaType> ITEM_TO_DIGITAMA = Map.ofEntries(
            Map.entry(ItemType.DIGITAMA_STARTER, DigitamaType.STARTER),
            Map.entry(ItemType.DIGITAMA_FIRE, DigitamaType.FIRE),
            Map.entry(ItemType.DIGITAMA_WATER, DigitamaType.WATER),
            Map.entry(ItemType.DIGITAMA_NATURE, DigitamaType.NATURE),
            Map.entry(ItemType.DIGITAMA_EARTH, DigitamaType.EARTH),
            Map.entry(ItemType.DIGITAMA_WIND, DigitamaType.WIND),
            Map.entry(ItemType.DIGITAMA_LIGHT, DigitamaType.LIGHT),
            Map.entry(ItemType.DIGITAMA_DARK, DigitamaType.DARK),
            Map.entry(ItemType.DIGITAMA_THUNDER, DigitamaType.THUNDER),
            Map.entry(ItemType.DIGITAMA_NEUTRAL, DigitamaType.NEUTRAL),
            Map.entry(ItemType.DIGITAMA_ICE, DigitamaType.ICE),
            Map.entry(ItemType.DIGITAMA_STEEL, DigitamaType.STEEL)
    );

    private static final Map<DigitamaType, List<String>> HATCH_TABLE = Map.ofEntries(
            Map.entry(DigitamaType.STARTER, List.of("Botamon", "Pichimon", "Pabumon", "Punimon", "Poyomon", "Yuramon")),
            Map.entry(DigitamaType.FIRE, List.of("Bombmon", "Bommon", "Jyarimon", "Mokumon", "Peti Meramon")),
            Map.entry(DigitamaType.WATER, List.of("Punimon", "Pichimon", "Bubbmon", "Fukamon", "Kekomon", "Pitchmon", "Pururumon", "Puyomon")),
            Map.entry(DigitamaType.NATURE, List.of("Yuramon", "Leafmon", "Nyokimon", "Popomon")),
            Map.entry(DigitamaType.EARTH, List.of("Cotsucomon", "Sakumon", "Sunamon", "Tsubumon")),
            Map.entry(DigitamaType.WIND, List.of("Pabumon", "Chibickmon", "Fusamon", "Pafumon", "Pipimon", "Pupumon", "Puwamon")),
            Map.entry(DigitamaType.LIGHT, List.of("Poyomon", "Chicomon", "Fufumon", "Ketomon", "Petitmon", "Pusumon", "Puttimon", "Relemon", "Yolkmon")),
            Map.entry(DigitamaType.DARK, List.of("Algomon Baby I", "Dodomon", "Keemon", "Kuramon", "Zurumon")),
            Map.entry(DigitamaType.THUNDER, List.of("Choromon", "Dokimon")),
            Map.entry(DigitamaType.NEUTRAL, List.of("Botamon", "Cocomon", "Curimon", "Paomon", "Pyonmon", "Tomorimon", "Torikara Ballmon", "Zerimon")),
            Map.entry(DigitamaType.ICE, List.of("Yukimi Botamon")),
            Map.entry(DigitamaType.STEEL, List.of("MetalKoromon"))
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
