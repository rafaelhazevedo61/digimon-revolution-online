package com.dro.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Centralizes gameplay settings that can vary between environments without
 * changing domain rules in source code.
 */
@Component
@ConfigurationProperties(prefix = "dro.gameplay")
public class GameplayConfig {

    private Energy energy = new Energy();
    private Arena arena = new Arena();
    private WorldBoss worldBoss = new WorldBoss();
    private ClanRaid clanRaid = new ClanRaid();
    private boolean autoBossRespawnAfterDefeatEnabled = false;
    private boolean bossCooldownEnabled = true;
    private ActivityCalendar activityCalendar = new ActivityCalendar();

    public Energy getEnergy() {
        return energy;
    }

    public void setEnergy(Energy energy) {
        this.energy = energy;
    }

    public Arena getArena() {
        return arena;
    }

    public void setArena(Arena arena) {
        this.arena = arena;
    }

    public WorldBoss getWorldBoss() {
        return worldBoss;
    }

    public void setWorldBoss(WorldBoss worldBoss) {
        this.worldBoss = worldBoss;
    }

    public ClanRaid getClanRaid() {
        return clanRaid;
    }

    public void setClanRaid(ClanRaid clanRaid) {
        this.clanRaid = clanRaid;
    }

    public boolean isAutoBossRespawnAfterDefeatEnabled() {
        return autoBossRespawnAfterDefeatEnabled;
    }

    public void setAutoBossRespawnAfterDefeatEnabled(boolean autoBossRespawnAfterDefeatEnabled) {
        this.autoBossRespawnAfterDefeatEnabled = autoBossRespawnAfterDefeatEnabled;
    }

    public ActivityCalendar getActivityCalendar() { return activityCalendar; }

    public void setActivityCalendar(ActivityCalendar activityCalendar) { this.activityCalendar = activityCalendar; }

    public boolean isBossCooldownEnabled() {
        return bossCooldownEnabled;
    }

    public void setBossCooldownEnabled(boolean bossCooldownEnabled) {
        this.bossCooldownEnabled = bossCooldownEnabled;
    }

    public boolean isWorldBossCooldownEnabled() {
        return bossCooldownEnabled && worldBoss.isCooldownEnabled();
    }

    public boolean isClanRaidCooldownEnabled() {
        return bossCooldownEnabled && clanRaid.isCooldownEnabled();
    }

    public boolean isEnergyConsumptionEnabled() {
        return energy.isConsumptionEnabled();
    }

    public int getArenaDailyChallengeLimit() {
        return arena.getDailyChallengeLimit();
    }

    public static class ActivityCalendar {
        private boolean enabled = true;
        private int dailyGoal = 10;
        private String rewardChestCode = "CHEST_ACTIVITY_CALENDAR";
        private String monthlyCompletionChestCode = "CHEST_ACTIVITY_CALENDAR_MONTHLY";
        private int missionCompleted = 1;
        private int arenaMatch = 1;
        private int clanRaidAttack = 1;
        private int worldBossAttack = 1;
        private int bossChallenge = 1;
        private int digitamaHatched = 1;
        private int missionLimit;
        private int arenaLimit;
        private int clanRaidLimit;
        private int worldBossLimit;
        private int bossLimit;
        private int hatchLimit;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getDailyGoal() { return dailyGoal; }
        public void setDailyGoal(int dailyGoal) { this.dailyGoal = dailyGoal; }
        public String getRewardChestCode() { return rewardChestCode; }
        public void setRewardChestCode(String value) { this.rewardChestCode = value; }
        public String getMonthlyCompletionChestCode() { return monthlyCompletionChestCode; }
        public void setMonthlyCompletionChestCode(String value) { this.monthlyCompletionChestCode = value; }
        public int getMissionCompleted() { return missionCompleted; }
        public void setMissionCompleted(int value) { this.missionCompleted = value; }
        public int getArenaMatch() { return arenaMatch; }
        public void setArenaMatch(int value) { this.arenaMatch = value; }
        public int getClanRaidAttack() { return clanRaidAttack; }
        public void setClanRaidAttack(int value) { this.clanRaidAttack = value; }
        public int getWorldBossAttack() { return worldBossAttack; }
        public void setWorldBossAttack(int value) { this.worldBossAttack = value; }
        public int getBossChallenge() { return bossChallenge; }
        public void setBossChallenge(int value) { this.bossChallenge = value; }
        public int getDigitamaHatched() { return digitamaHatched; }
        public void setDigitamaHatched(int value) { this.digitamaHatched = value; }
        public int getMissionLimit() { return missionLimit; }
        public void setMissionLimit(int value) { this.missionLimit = value; }
        public int getArenaLimit() { return arenaLimit; }
        public void setArenaLimit(int value) { this.arenaLimit = value; }
        public int getClanRaidLimit() { return clanRaidLimit; }
        public void setClanRaidLimit(int value) { this.clanRaidLimit = value; }
        public int getWorldBossLimit() { return worldBossLimit; }
        public void setWorldBossLimit(int value) { this.worldBossLimit = value; }
        public int getBossLimit() { return bossLimit; }
        public void setBossLimit(int value) { this.bossLimit = value; }
        public int getHatchLimit() { return hatchLimit; }
        public void setHatchLimit(int value) { this.hatchLimit = value; }
    }

    public static class Energy {

        private boolean consumptionEnabled = true;

        public boolean isConsumptionEnabled() {
            return consumptionEnabled;
        }

        public void setConsumptionEnabled(boolean consumptionEnabled) {
            this.consumptionEnabled = consumptionEnabled;
        }
    }

    public static class Arena {

        private int dailyChallengeLimit = 5;

        public int getDailyChallengeLimit() {
            return dailyChallengeLimit;
        }

        public void setDailyChallengeLimit(int dailyChallengeLimit) {
            this.dailyChallengeLimit = dailyChallengeLimit;
        }
    }

    public static class WorldBoss {

        private boolean cooldownEnabled = true;

        public boolean isCooldownEnabled() {
            return cooldownEnabled;
        }

        public void setCooldownEnabled(boolean cooldownEnabled) {
            this.cooldownEnabled = cooldownEnabled;
        }
    }

    public static class ClanRaid {

        private boolean cooldownEnabled = true;

        public boolean isCooldownEnabled() {
            return cooldownEnabled;
        }

        public void setCooldownEnabled(boolean cooldownEnabled) {
            this.cooldownEnabled = cooldownEnabled;
        }
    }
}
