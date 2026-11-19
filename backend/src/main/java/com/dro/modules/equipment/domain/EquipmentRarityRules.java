package com.dro.modules.equipment.domain;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Componente da camada de conjunto de regras de domínio do módulo de Equipamentos.
 */
public class EquipmentRarityRules {

    public record RarityProfile(int common, int rare, int epic, int legendary) {
        public RarityProfile {
            int total = common + rare + epic + legendary;
            if (total != 100) {
                throw new IllegalArgumentException(
                        "Rarity profile must sum to 100, got " + total);
            }
        }

        public RarityProfile withQualityBonus(int bonusPoints) {
            int cappedBonus = Math.min(bonusPoints, common());
            return new RarityProfile(common() - cappedBonus, rare(), epic(), legendary() + cappedBonus);
        }
    }

    private static final RarityProfile DEFAULT = new RarityProfile(60, 25, 12, 3);

    // -- Perfis por contexto --------------------------------------------------
    private static final RarityProfile BOSS_NORMAL = new RarityProfile(65, 22, 10, 3);
    private static final RarityProfile BOSS_DAILY = new RarityProfile(55, 28, 13, 4);
    private static final RarityProfile BOSS_WEEKLY = new RarityProfile(40, 30, 20, 10);
    private static final RarityProfile BOSS_MONTHLY = new RarityProfile(20, 30, 30, 20);
    private static final RarityProfile SHOP = new RarityProfile(100, 0, 0, 0);

    private static final Map<String, RarityProfile> PROFILES = Map.of(
            "DEFAULT", DEFAULT,
            "BOSS_NORMAL", BOSS_NORMAL,
            "BOSS_DAILY", BOSS_DAILY,
            "BOSS_WEEKLY", BOSS_WEEKLY,
            "BOSS_MONTHLY", BOSS_MONTHLY,
            "SHOP", SHOP
    );

    public static EquipmentRarity rollRarity() {
        return rollRarity(DEFAULT);
    }

    public static EquipmentRarity rollRarity(String profileName) {
        RarityProfile profile = PROFILES.get(profileName);
        if (profile == null) {
            throw new IllegalArgumentException("Unknown rarity profile: " + profileName);
        }
        return rollRarity(profile);
    }

    public static EquipmentRarity rollRarity(RarityProfile profile) {
        return rollRarity(profile, 0.0);
    }

    public static EquipmentRarity rollRarity(String profileName, double qualityBonusPercent) {
        RarityProfile profile = PROFILES.get(profileName);
        if (profile == null) {
            throw new IllegalArgumentException("Unknown rarity profile: " + profileName);
        }
        return rollRarity(profile, qualityBonusPercent);
    }

    public static EquipmentRarity rollRarity(RarityProfile profile, double qualityBonusPercent) {
        int bonusPoints = (int) Math.round(qualityBonusPercent * 100);
        RarityProfile effective = bonusPoints > 0 ? profile.withQualityBonus(bonusPoints) : profile;

        int roll = ThreadLocalRandom.current().nextInt(1, 101);

        if (roll <= effective.legendary()) return EquipmentRarity.LEGENDARY;
        if (roll <= effective.legendary() + effective.epic()) return EquipmentRarity.EPIC;
        if (roll <= effective.legendary() + effective.epic() + effective.rare()) return EquipmentRarity.RARE;
        return EquipmentRarity.COMMON;
    }

    public static RarityProfile getProfile(String profileName) {
        return PROFILES.get(profileName);
    }
}
