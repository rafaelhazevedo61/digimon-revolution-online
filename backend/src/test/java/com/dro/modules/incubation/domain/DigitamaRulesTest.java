package com.dro.modules.incubation.domain;

import com.dro.modules.inventory.domain.ItemType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DigitamaRulesTest {

    @Test
    void isDigitama_digitamaFire_returnsTrue() {
        assertTrue(DigitamaRules.isDigitama(ItemType.DIGITAMA_FIRE));
    }

    @Test
    void isDigitama_digitamaWater_returnsTrue() {
        assertTrue(DigitamaRules.isDigitama(ItemType.DIGITAMA_WATER));
    }

    @Test
    void isDigitama_digitamaNature_returnsTrue() {
        assertTrue(DigitamaRules.isDigitama(ItemType.DIGITAMA_NATURE));
    }

    @Test
    void isDigitama_nonDigitamaItem_returnsFalse() {
        assertFalse(DigitamaRules.isDigitama(ItemType.FRAGMENT_CHAMPION));
    }

    @Test
    void isDigitama_dataCoreItem_returnsFalse() {
        assertFalse(DigitamaRules.isDigitama(ItemType.DATA_CORE));
    }
}
