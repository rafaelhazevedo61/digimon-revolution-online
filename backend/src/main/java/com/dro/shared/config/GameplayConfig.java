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

    public int getWorldBossDailyAttackLimit() {
        return worldBoss.getDailyAttackLimit();
    }

    public int getClanRaidDailyAttackLimit() {
        return clanRaid.getDailyAttackLimit();
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

        private int dailyAttackLimit = 3;
        private boolean cooldownEnabled = true;

        public int getDailyAttackLimit() {
            return dailyAttackLimit;
        }

        public void setDailyAttackLimit(int dailyAttackLimit) {
            this.dailyAttackLimit = dailyAttackLimit;
        }

        public boolean isCooldownEnabled() {
            return cooldownEnabled;
        }

        public void setCooldownEnabled(boolean cooldownEnabled) {
            this.cooldownEnabled = cooldownEnabled;
        }
    }

    public static class ClanRaid {

        private int dailyAttackLimit = 3;
        private boolean cooldownEnabled = true;

        public int getDailyAttackLimit() {
            return dailyAttackLimit;
        }

        public void setDailyAttackLimit(int dailyAttackLimit) {
            this.dailyAttackLimit = dailyAttackLimit;
        }

        public boolean isCooldownEnabled() {
            return cooldownEnabled;
        }

        public void setCooldownEnabled(boolean cooldownEnabled) {
            this.cooldownEnabled = cooldownEnabled;
        }
    }
}
