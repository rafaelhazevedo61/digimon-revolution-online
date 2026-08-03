package com.dro.modules.arena.domain;

import com.dro.modules.digimon.domain.enums.Stage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArenaRulesTest {

    @Test
    void initialRatingIsThousand() {
        assertEquals(1000, ArenaRules.INITIAL_RATING);
    }

    @Test
    void expectedScoreIsHalfForEqualRatings() {
        assertEquals(0.5, ArenaRules.expectedScore(1000, 1000), 1e-9);
    }

    @Test
    void expectedScoreIsHigherForStrongerPlayer() {
        double stronger = ArenaRules.expectedScore(1200, 1000);
        double weaker = ArenaRules.expectedScore(1000, 1200);
        assertTrue(stronger > 0.5);
        assertTrue(weaker < 0.5);
        assertEquals(1.0, stronger + weaker, 1e-9);
    }

    @Test
    void newRatingGoesUpOnWinAndDownOnLoss() {
        double expected = ArenaRules.expectedScore(1000, 1000);
        int afterWin = ArenaRules.newRating(1000, expected, 1);
        int afterLoss = ArenaRules.newRating(1000, expected, 0);
        assertEquals(1016, afterWin); // 1000 + 32 * (1 - 0.5)
        assertEquals(984, afterLoss); // 1000 + 32 * (0 - 0.5)
    }

    @Test
    void newRatingNeverGoesBelowMinimum() {
        int result = ArenaRules.newRating(ArenaRules.MIN_RATING, 1.0, 0);
        assertEquals(ArenaRules.MIN_RATING, result);
        assertTrue(result >= ArenaRules.MIN_RATING);
    }

    @Test
    void withinChallengeWindowRespectsRatingWindow() {
        assertTrue(ArenaRules.withinChallengeWindow(1000, 1200));
        assertTrue(ArenaRules.withinChallengeWindow(1000, 800));
        assertFalse(ArenaRules.withinChallengeWindow(1000, 1201));
        assertFalse(ArenaRules.withinChallengeWindow(1000, 799));
    }

    @Test
    void withinStageRangeAllowsSameStage() {
        assertTrue(ArenaRules.withinStageRange(Stage.ROOKIE, Stage.ROOKIE));
    }

    @Test
    void withinStageRangeAllowsAdjacentStage() {
        assertTrue(ArenaRules.withinStageRange(Stage.ROOKIE, Stage.CHAMPION));
        assertTrue(ArenaRules.withinStageRange(Stage.CHAMPION, Stage.ROOKIE));
    }

    @Test
    void withinStageRangeRejectsFarStages() {
        assertFalse(ArenaRules.withinStageRange(Stage.ROOKIE, Stage.ULTIMATE));
        assertFalse(ArenaRules.withinStageRange(Stage.BABY, Stage.MEGA));
    }

    @Test
    void winBitsProportionalToRatingDifference() {
        // Oponente 200 pontos acima rende mais.
        assertEquals(150, ArenaRules.winBits(1000, 1200));
        // Ratings iguais rendem a base.
        assertEquals(100, ArenaRules.winBits(1000, 1000));
        // Oponente 200 pontos abaixo rende menos.
        assertEquals(50, ArenaRules.winBits(1000, 800));
    }

    @Test
    void winBitsClampedToFloor() {
        assertEquals(ArenaRules.MIN_WIN_BITS, ArenaRules.winBits(2000, 100));
    }

    @Test
    void winBitsClampedToCeiling() {
        assertEquals(ArenaRules.MAX_WIN_BITS, ArenaRules.winBits(100, 2000));
    }

    @Test
    void remainingDailyChallengesNeverNegative() {
        assertEquals(ArenaRules.DAILY_CHALLENGE_LIMIT, ArenaRules.remainingDailyChallenges(0));
        assertEquals(0, ArenaRules.remainingDailyChallenges(ArenaRules.DAILY_CHALLENGE_LIMIT));
        assertEquals(0, ArenaRules.remainingDailyChallenges(ArenaRules.DAILY_CHALLENGE_LIMIT + 5));
    }

    @Test
    void winChanceIsFiftyForEqualPower() {
        assertEquals(50, ArenaRules.winChance(500.0, 500.0));
    }

    @Test
    void winChanceHigherForStrongerAttacker() {
        int strong = ArenaRules.winChance(900.0, 100.0);
        int weak = ArenaRules.winChance(100.0, 900.0);
        assertTrue(strong > 50);
        assertTrue(weak < 50);
        assertEquals(100, strong + weak);
    }

    @Test
    void winChanceIsClampedToBounds() {
        assertEquals(ArenaRules.MAX_WIN_CHANCE, ArenaRules.winChance(1_000_000.0, 1.0));
        assertEquals(ArenaRules.MIN_WIN_CHANCE, ArenaRules.winChance(1.0, 1_000_000.0));
    }

    @Test
    void winChanceDefaultsToFiftyWhenNoPower() {
        assertEquals(50, ArenaRules.winChance(0.0, 0.0));
    }

    @Test
    void dailyLimitReachedWhenUsedMeetsLimit() {
        assertFalse(ArenaRules.dailyLimitReached(ArenaRules.DAILY_CHALLENGE_LIMIT - 1));
        assertTrue(ArenaRules.dailyLimitReached(ArenaRules.DAILY_CHALLENGE_LIMIT));
        assertTrue(ArenaRules.dailyLimitReached(ArenaRules.DAILY_CHALLENGE_LIMIT + 1));
    }
}
