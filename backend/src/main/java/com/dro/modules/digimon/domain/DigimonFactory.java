package com.dro.modules.digimon.domain;

import com.dro.modules.digitama.domain.enums.DigitamaType;
import com.dro.modules.digimon.domain.enums.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

public class DigimonFactory {

    private static final Random random = new Random();
    private static final double HP_IV_WEIGHT = 0.30;
    private static final double ATTACK_IV_WEIGHT = 0.20;
    private static final double DEFENSE_IV_WEIGHT = 0.20;

    public static Digimon createBaby(UUID playerId, DigitamaType digitamaType,
                                     DigimonInfos digimon) {

//        String babyName = DigitamaHatchRules.rollBabyName(digitamaType);
//        String type = digitamaType.name();

        Rarity rarity = RarityRoller.roll();

        Personality personality = PersonalityRoller.roll();

        Trait trait = TraitRoller.rollForNormalHatch();

        int minIv = RarityRules.getMinimumIv(rarity);

        int ivHp = rollIv(minIv);
        int ivAttack = rollIv(minIv);
        int ivDefense = rollIv(minIv);

        DigimonGrade grade = DigimonGradeRules.calculate(ivHp, ivAttack, ivDefense);

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

        int hp = (int) Math.floor((digimon.getBaseHp() + (ivHp * HP_IV_WEIGHT)) * hpMultiplier);
        int attack = (int) Math.floor((digimon.getBaseAtk() + (ivAttack * ATTACK_IV_WEIGHT)) * attackMultiplier);
        int defense = (int) Math.floor((digimon.getBaseDef() + (ivDefense * DEFENSE_IV_WEIGHT)) * defenseMultiplier);
        int maxEnergy = 20 + TraitRules.getMaxEnergyBonus(trait);

        return Digimon.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .name(digimon.getName())
                .type(digitamaType.name()) //digitama origem
                .stage(Stage.BABY)
                .rarity(rarity)
                .personality(personality)
                .level(1)
                .experience(0)
                .ivHp(ivHp)
                .ivAttack(ivAttack)
                .ivDefense(ivDefense)
                .grade(grade)
                .hp(hp)
                .attack(attack)
                .defense(defense)
                .createdAt(LocalDateTime.now())
                .energy(maxEnergy)
                .maxEnergy(maxEnergy)
                .trait(trait)
                .lastEnergyUpdate(Instant.now())
                .status(DigimonStatus.ACTIVE)
                .digimonInfoId(digimon.getId())
                .build();
    }

    private static int rollIv(int minIv) {
        return minIv + random.nextInt(101 - minIv);
    }
}