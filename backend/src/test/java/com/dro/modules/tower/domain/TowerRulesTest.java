package com.dro.modules.tower.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static com.dro.modules.tower.domain.TowerRules.*;

class TowerRulesTest {
    @Test void fixedFloorAndMilestones() {
        assertEquals(floor(17), floor(17));
        assertEquals("COMUM", floor(1).kind());
        assertEquals("CHEFE", floor(5).kind());
        assertEquals("MARCO", floor(10).kind());
        assertEquals("ENERGY_CELL", floor(1).reward().itemCode());
        assertEquals("VIP_ENERGY_CHARGE", floor(5).reward().itemCode());
        assertEquals("ASCENSION_CORE", floor(10).reward().itemCode());
    }
    @Test void fourProfilesAndGrowingCycles() {
        assertEquals(4, java.util.stream.LongStream.rangeClosed(1, 4).mapToObj(n -> floor(n).profile()).distinct().count());
        for (long n = 1; n < 100; n++) assertTrue(floor(n + 20).stats().hp().compareTo(floor(n).stats().hp()) > 0);
    }
    @Test void highFloorsDoNotOverflowAndRewardsAreCapped() {
        var high = floor(Long.MAX_VALUE);
        assertTrue(high.stats().hp().compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0);
        assertTrue(high.reward().bits() > 0 && high.reward().bits() <= 10000);
        assertEquals(1, high.reward().quantity());
        assertEquals(10000, floor(Long.MAX_VALUE - 7).reward().bits());
    }
    @Test void teamCompositionChangesOutcome() {
        var member = Stats.of(16, 4, 6);
        assertFalse(fight(List.of(member), floor(1).stats()).won());
        assertTrue(fight(List.of(member, member, member), floor(1).stats()).won());
    }
    @Test void winningHitHasNoRetaliation() {
        var result = fight(List.of(Stats.of(1, 1000, 0)), Stats.of(10, 10000, 0));
        assertTrue(result.won()); assertEquals(1, result.rounds());
        assertEquals(List.of(BigDecimal.ONE), result.remainingHp());
    }
    @Test void fightsStopAtSixtyRounds() {
        var result = fight(List.of(Stats.of(100000, 1, 1000)), Stats.of(100000, 1, 1000));
        assertFalse(result.won()); assertEquals(60, result.rounds());
    }
    @Test void remainingHealthFollowsFormationAndClampsOverkill() {
        var result = fight(List.of(Stats.of(5, 4, 0), Stats.of(100, 4, 0)), Stats.of(14, 10, 0));
        assertTrue(result.won());
        assertEquals(List.of(BigDecimal.ZERO, BigDecimal.valueOf(90)), result.remainingHp());
    }
    @Test void defeatReportsZeroForEveryFallenMember() {
        var result = fight(List.of(Stats.of(1, 1, 0), Stats.of(1, 1, 0)), Stats.of(100, 1000, 0));
        assertFalse(result.won());
        assertEquals(List.of(BigDecimal.ZERO, BigDecimal.ZERO), result.remainingHp());
    }
    @Test void timeoutPreservesSurvivingHealth() {
        var result = fight(List.of(Stats.of(100000, 1, 1000)), Stats.of(100000, 1, 1000));
        assertFalse(result.won());
        assertEquals(List.of(BigDecimal.valueOf(99940)), result.remainingHp());
    }
    @Test void invalidFloorAndTeamAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> floor(0));
        assertThrows(IllegalArgumentException.class, () -> fight(List.of(), floor(1).stats()));
    }
}
