package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Rarity;

import java.util.Optional;
import java.util.Random;

/**
 * Responsável por sortear a raridade de um Digimon.
 *
 * Existem dois fluxos principais:
 *
 * 1. Hatch comum:
 *    - Usado quando o jogador choca um Digitama normal.
 *    - A raridade é sorteada com os pesos base.
 *
 * 2. Rebirth:
 *    - Usado quando um Digimon renasce.
 *    - Primeiro existe uma chance de herdar a raridade anterior.
 *    - Caso a herança falhe, uma nova raridade é sorteada.
 *    - Quanto maior o número de Rebirths, melhores ficam os pesos do sorteio.
 *
 * Regras atuais:
 *
 * - Chance de herdar raridade no Rebirth:
 *   40% + 2% por Rebirth, com limite máximo de 70%.
 *
 * - Pesos dinâmicos:
 *   COMMON diminui conforme o número de Rebirths.
 *   RARE aumenta conforme o número de Rebirths.
 *   EPIC aumenta levemente.
 *   LEGENDARY aumenta lentamente e continua raro.
 *
 * Objetivo de design:
 *
 * - Valorizar Digimons raros antigos.
 * - Evitar que o Rebirth seja 100% aleatório e frustrante.
 * - Evitar que raridade alta seja garantida.
 * - Manter emoção no nascimento do novo Digimon.
 * - Permitir progressão infinita sem quebrar o balanceamento.
 */
public class RarityRoller {

    private static final Random random = new Random();

    public static Rarity roll() {
        return rollByDynamicWeights(0);
    }

    /**
     * Sorteio explícito do Dado de Raridade. O resultado nunca repete a raridade
     * atual. Para Digimons Comuns, existe também a possibilidade de o dado não
     * alterar a raridade.
     */
    public static Optional<Rarity> rollForRarityDie(Rarity currentRarity) {
        int roll = random.nextInt(10000);
        return switch (currentRarity) {
            case COMMON -> {
                if (roll < 7000) yield Optional.empty();
                if (roll < 9800) yield Optional.of(Rarity.RARE);
                if (roll < 9980) yield Optional.of(Rarity.EPIC);
                yield Optional.of(Rarity.LEGENDARY);
            }
            case RARE -> {
                if (roll < 9800) yield Optional.of(Rarity.COMMON);
                if (roll < 9980) yield Optional.of(Rarity.EPIC);
                yield Optional.of(Rarity.LEGENDARY);
            }
            case EPIC -> {
                if (roll < 7129) yield Optional.of(Rarity.COMMON);
                if (roll < 9980) yield Optional.of(Rarity.RARE);
                yield Optional.of(Rarity.LEGENDARY);
            }
            case LEGENDARY -> {
                if (roll < 6500) yield Optional.of(Rarity.COMMON);
                if (roll < 9000) yield Optional.of(Rarity.RARE);
                yield Optional.of(Rarity.EPIC);
            }
        };
    }

    /**
     * Sorteia uma raridade específica para o fluxo de Rebirth.
     *
     * Primeiro tenta herdar a raridade anterior.
     * Se não herdar, faz um novo sorteio usando pesos dinâmicos.
     */
    public static Rarity rollForRebirth(Rarity previousRarity, int rebirthCount) {

        if (shouldInheritRarity(rebirthCount)) {
            return previousRarity;
        }

        return rollByDynamicWeights(rebirthCount);
    }

    private static boolean shouldInheritRarity(int rebirthCount) {

        double inheritChance = 0.40 + (rebirthCount * 0.02);

        inheritChance = Math.min(inheritChance, 0.70);

        return random.nextDouble() < inheritChance;
    }

    private static Rarity rollByDynamicWeights(int rebirthCount) {

        int cappedRebirth = Math.min(rebirthCount, 25);

        int legendaryBonus = Math.min(cappedRebirth / 10, 5);

        int commonWeight = Math.max(20, 70 - (cappedRebirth * 2));
        int rareWeight = 20 + cappedRebirth;
        int epicWeight = 8 + (cappedRebirth / 2);
        int legendaryWeight = 2 + legendaryBonus;

        return rollByWeights(commonWeight, rareWeight, epicWeight, legendaryWeight);
    }

    private static Rarity rollByWeights(int commonWeight, int rareWeight, int epicWeight, int legendaryWeight) {
        int totalWeight = commonWeight + rareWeight + epicWeight + legendaryWeight;
        int roll = random.nextInt(totalWeight) + 1;
        if (roll <= commonWeight) return Rarity.COMMON;
        roll -= commonWeight;
        if (roll <= rareWeight) return Rarity.RARE;
        roll -= rareWeight;
        if (roll <= epicWeight) return Rarity.EPIC;
        return Rarity.LEGENDARY;
    }
}