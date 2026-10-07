package com.dro.modules.boss.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BossDefinitionEntityTest {

    @Test
    void combatPowerFallsBackToHpAtkDefWhenCombatStatsAreMissing() {
        BossDefinitionEntity boss = BossDefinitionEntity.builder().hp(500).atk(80).def(50).build();

        assertEquals(BossCombatRules.calculatePower(500, 80, 50), boss.combatPower());
    }

    @Test
    void combatPowerUsesCombatStatsInsteadOfLifeHp() {
        BossDefinitionEntity boss = BossDefinitionEntity.builder()
                .hp(1_000_000).atk(5000).def(4000)
                .combatHp(1000).combatAtk(300).combatDef(250)
                .build();

        assertEquals(1000.0, boss.combatPower(), 0.0001);
        assertEquals(1_000_000, boss.getHp());
    }

    @Test
    void combatPowerFallsBackPerAttribute() {
        BossDefinitionEntity boss = BossDefinitionEntity.builder().hp(100).atk(10).def(10).combatHp(600).build();

        assertEquals(BossCombatRules.calculatePower(600, 10, 10), boss.combatPower());
    }
}
