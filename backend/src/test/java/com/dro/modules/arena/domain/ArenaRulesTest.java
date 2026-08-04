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
    void winArenaCoinsRewardsHarderWinsMore() {
        int easyWin = ArenaRules.winArenaCoins(95);
        int coinFlip = ArenaRules.winArenaCoins(50);
        int hardWin = ArenaRules.winArenaCoins(5);
        assertTrue(hardWin > coinFlip);
        assertTrue(coinFlip > easyWin);
    }

    @Test
    void winArenaCoinsClampedToBounds() {
        assertEquals(ArenaRules.WIN_ARENA_COINS_MIN, ArenaRules.winArenaCoins(100));
        for (int chance = 0; chance <= 100; chance++) {
            int coins = ArenaRules.winArenaCoins(chance);
            assertTrue(coins >= ArenaRules.WIN_ARENA_COINS_MIN);
            assertTrue(coins <= ArenaRules.WIN_ARENA_COINS_MAX);
        }
    }

    @Test
    void lossStillGrantsParticipationCoins() {
        assertEquals(ArenaRules.LOSS_ARENA_COINS, ArenaRules.lossArenaCoins());
        assertTrue(ArenaRules.lossArenaCoins() > 0);
        assertTrue(ArenaRules.lossArenaCoins() < ArenaRules.WIN_ARENA_COINS_MIN);
    }

    @Test
    void tierForMapsRatingToBand() {
        assertEquals(ArenaTier.BRONZE, ArenaRules.tierFor(0));
        assertEquals(ArenaTier.BRONZE, ArenaRules.tierFor(899));
        assertEquals(ArenaTier.PRATA, ArenaRules.tierFor(900));
        assertEquals(ArenaTier.PRATA, ArenaRules.tierFor(1099));
        assertEquals(ArenaTier.OURO, ArenaRules.tierFor(1100));
        assertEquals(ArenaTier.OURO, ArenaRules.tierFor(1299));
        assertEquals(ArenaTier.PLATINA, ArenaRules.tierFor(1300));
        assertEquals(ArenaTier.PLATINA, ArenaRules.tierFor(1499));
        assertEquals(ArenaTier.DIAMANTE, ArenaRules.tierFor(1500));
        assertEquals(ArenaTier.DIAMANTE, ArenaRules.tierFor(5000));
    }

    @Test
    void pointsToNextTierCountsRemainingRating() {
        assertEquals(100, ArenaRules.pointsToNextTier(800)); // bronze -> prata em 900
        assertEquals(1, ArenaRules.pointsToNextTier(1099)); // prata -> ouro em 1100
        assertEquals(0, ArenaRules.pointsToNextTier(1600)); // diamante (topo)
    }

    @Test
    void nextTierIsNullOnlyAtTop() {
        assertEquals(ArenaTier.DIAMANTE, ArenaTier.PLATINA.next());
        assertNull(ArenaTier.DIAMANTE.next());
    }

    @Test
    void dailyLimitReachedWhenUsedMeetsLimit() {
        assertFalse(ArenaRules.dailyLimitReached(ArenaRules.DAILY_CHALLENGE_LIMIT - 1));
        assertTrue(ArenaRules.dailyLimitReached(ArenaRules.DAILY_CHALLENGE_LIMIT));
        assertTrue(ArenaRules.dailyLimitReached(ArenaRules.DAILY_CHALLENGE_LIMIT + 1));
    }
}
