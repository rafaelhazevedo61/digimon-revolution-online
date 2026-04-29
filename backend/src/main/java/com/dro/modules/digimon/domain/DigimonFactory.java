package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Personality;
import com.dro.modules.digimon.domain.enums.Rarity;
import com.dro.modules.digimon.domain.enums.Stage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

public class DigimonFactory {

    private static final Random random = new Random();

    private DigimonFactory() {
    }

    public static Digimon createBaby(UUID playerId, Enum<?> digitamaType) {
        return createBaby(playerId, digitamaType.name());
    }

    public static Digimon createBaby(UUID playerId, String digitamaType) {

        Rarity rarity = RarityRoller.roll();

        Personality personality = PersonalityRoller.roll();

        int minIv = RarityRules.getMinimumIv(rarity);

        int ivHp = rollIv(minIv);
        int ivAttack = rollIv(minIv);
        int ivDefense = rollIv(minIv);

        double rarityMultiplier = RarityRules.getStatMultiplier(rarity);
        double stageMultiplier = EvolutionRules.stageStatMultiplier(Stage.BABY);

        double hpMultiplier =
                rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getHpMultiplier(personality);

        double attackMultiplier =
                rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getAttackMultiplier(personality);

        double defenseMultiplier =
                rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getDefenseMultiplier(personality);

        int hp = (int) Math.floor((10 + ivHp) * hpMultiplier);
        int attack = (int) Math.floor((5 + ivAttack) * attackMultiplier);
        int defense = (int) Math.floor((5 + ivDefense) * defenseMultiplier);

        return Digimon.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .name("Baby " + digitamaType)
                .type(digitamaType)
                .stage(Stage.BABY)
                .rarity(rarity)
                .personality(personality)
                .level(1)
                .experience(0)
                .ivHp(ivHp)
                .ivAttack(ivAttack)
                .ivDefense(ivDefense)
                .hp(hp)
                .attack(attack)
                .defense(defense)
                .createdAt(LocalDateTime.now())
                .energy(20)
                .maxEnergy(20)
                .lastEnergyUpdate(Instant.now())
                .build();
    }

    private static int rollIv(int minIv) {
        return minIv + random.nextInt(101 - minIv);
    }
}