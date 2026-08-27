package com.dro.modules.digitama.application;

import com.dro.modules.digitama.config.DigitamaConfig;
import com.dro.modules.digitama.domain.DigitamaPool;
import com.dro.modules.digitama.domain.DigitamaPoolEntry;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.domain.enums.Element;
import com.dro.modules.digimon.domain.enums.Stage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DigitamaPoolEligibilityServiceTest {

    @Test
    void acceptsOnlyBabyDigimonsFromThePoolElementAndEnabledByConfig() {
        DigitamaConfig config = new DigitamaConfig();
        config.setBabyDigimons(Map.of(
                "Fire Baby", true,
                "Disabled Fire Baby", false,
                "Water Baby", true,
                "Rookie Fire", true
        ));
        DigitamaPoolEligibilityService service = new DigitamaPoolEligibilityService(config);

        DigitamaPoolEntry fireBaby = entry(info("Fire Baby", Stage.BABY, Element.FIRE), true, 50);
        DigitamaPoolEntry disabledFireBaby = entry(info("Disabled Fire Baby", Stage.BABY, Element.FIRE), true, 50);
        DigitamaPoolEntry waterBaby = entry(info("Water Baby", Stage.BABY, Element.WATER), true, 50);
        DigitamaPoolEntry rookieFire = entry(info("Rookie Fire", Stage.ROOKIE, Element.FIRE), true, 50);
        DigitamaPoolEntry inactiveFireBaby = entry(info("Fire Baby", Stage.BABY, Element.FIRE), false, 50);
        DigitamaPool pool = DigitamaPool.builder()
                .code("DIGITAMA_FIRE")
                .entries(List.of(fireBaby, disabledFireBaby, waterBaby, rookieFire, inactiveFireBaby))
                .build();

        assertEquals(List.of(fireBaby), service.getEligibleEntries(pool));
    }

    @Test
    void starterPoolAcceptsBabiesFromAnyElementWhenEnabled() {
        DigitamaConfig config = new DigitamaConfig();
        config.setBabyDigimons(Map.of("Fire Baby", true, "Water Baby", true));
        DigitamaPoolEligibilityService service = new DigitamaPoolEligibilityService(config);
        DigitamaPoolEntry fireBaby = entry(info("Fire Baby", Stage.BABY, Element.FIRE), true, 20);
        DigitamaPoolEntry waterBaby = entry(info("Water Baby", Stage.BABY, Element.WATER), true, 20);
        DigitamaPool pool = DigitamaPool.builder()
                .code("DIGITAMA_STARTER")
                .entries(List.of(fireBaby, waterBaby))
                .build();

        assertEquals(List.of(fireBaby, waterBaby), service.getEligibleEntries(pool));
    }

    private DigitamaPoolEntry entry(DigimonInfos info, boolean active, int weight) {
        return DigitamaPoolEntry.builder()
                .digimonInfo(info)
                .active(active)
                .weight(weight)
                .build();
    }

    private DigimonInfos info(String name, Stage stage, Element element) {
        return DigimonInfos.builder()
                .name(name)
                .stage(stage)
                .element(element)
                .build();
    }
}
