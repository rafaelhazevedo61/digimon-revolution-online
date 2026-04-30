package com.dro.modules.incubation.domain;

import com.dro.modules.inventory.domain.ItemType;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class IncubatorRulesTest {

    @Test
    void getIncubationTime_common_returns5Minutes() {
        assertEquals(Duration.ofMinutes(5), IncubatorRules.getIncubationTime(ItemType.INCUBATOR_COMMON));
    }

    @Test
    void getIncubationTime_rare_returns2Minutes() {
        assertEquals(Duration.ofMinutes(2), IncubatorRules.getIncubationTime(ItemType.INCUBATOR_RARE));
    }

    @Test
    void getIncubationTime_epic_returns30Seconds() {
        assertEquals(Duration.ofSeconds(30), IncubatorRules.getIncubationTime(ItemType.INCUBATOR_EPIC));
    }

    @Test
    void getIncubationTime_invalidType_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> IncubatorRules.getIncubationTime(ItemType.DATA_CORE));
    }
}
