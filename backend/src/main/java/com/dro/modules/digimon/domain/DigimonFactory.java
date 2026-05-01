package com.dro.modules.digimon.domain;

import com.dro.modules.digitama.domain.DigitamaHatchRules;
import com.dro.modules.digitama.domain.DigitamaType;
import com.dro.modules.digimon.domain.enums.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

public class DigimonFactory {

    private static final Random random = new Random();

    private DigimonFactory() {
    }

    public static Digimon createBaby(UUID playerId, DigitamaType digitamaType) {

        String babyName = DigitamaHatchRules.rollBabyName(digitamaType);
        String type = digitamaType.name();

        Rarity rarity = RarityRoller.roll();

        Personality personality = PersonalityRoller.roll();

        Trait trait = TraitRoller.rollForNormalHatch();

        int minIv = RarityRules.getMinimumIv(rarity);

        int ivHp = rollIv(minIv);
        int ivAttack = rollIv(minIv);
        int ivDefense = rollIv(minIv);

        double rarityMultiplier = RarityRules.getStatMultiplier(rarity);
        double stageMultiplier = EvolutionRules.stageStatMultiplier(Stage.BABY);

        double hpMultiplier =
                rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getHpMultiplier(personality)
                        * TraitRules.getHpMultiplier(trait);

        double attackMultiplier =
                rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getAttackMultiplier(personality)
                        * TraitRules.getAttackMultiplier(trait);

        double defenseMultiplier =
                rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getDefenseMultiplier(personality)
                        * TraitRules.getDefenseMultiplier(trait);

        int hp = (int) Math.floor((10 + ivHp) * hpMultiplier);
        int attack = (int) Math.floor((5 + ivAttack) * attackMultiplier);
        int defense = (int) Math.floor((5 + ivDefense) * defenseMultiplier);
        int maxEnergy = 20 + TraitRules.getMaxEnergyBonus(trait);

        return Digimon.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .name(babyName)
                .type(type)
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
                .energy(maxEnergy)
                .maxEnergy(maxEnergy)
                .trait(trait)
                .lastEnergyUpdate(Instant.now())
                .status(DigimonStatus.ACTIVE)
                .build();
    }

    private static int rollIv(int minIv) {
        return minIv + random.nextInt(101 - minIv);
    }
}