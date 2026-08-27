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
    void isDigitama_allElementalDigitamas_returnsTrue() {
        assertTrue(DigitamaRules.isDigitama(ItemType.DIGITAMA_STARTER));
        assertTrue(DigitamaRules.isDigitama(ItemType.DIGITAMA_FIRE));
        assertTrue(DigitamaRules.isDigitama(ItemType.DIGITAMA_WATER));
        assertTrue(DigitamaRules.isDigitama(ItemType.DIGITAMA_NATURE));
        assertTrue(DigitamaRules.isDigitama(ItemType.DIGITAMA_EARTH));
        assertTrue(DigitamaRules.isDigitama(ItemType.DIGITAMA_WIND));
        assertTrue(DigitamaRules.isDigitama(ItemType.DIGITAMA_LIGHT));
        assertTrue(DigitamaRules.isDigitama(ItemType.DIGITAMA_DARK));
        assertTrue(DigitamaRules.isDigitama(ItemType.DIGITAMA_THUNDER));
        assertTrue(DigitamaRules.isDigitama(ItemType.DIGITAMA_NEUTRAL));
        assertTrue(DigitamaRules.isDigitama(ItemType.DIGITAMA_ICE));
        assertTrue(DigitamaRules.isDigitama(ItemType.DIGITAMA_STEEL));
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
