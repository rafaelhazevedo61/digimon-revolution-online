package com.dro.modules.tower.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.ArrayList;

/** Encontros fixos por andar, sem depender do poder do jogador. */
public final class TowerRules {
    private TowerRules() {}
    public static final int COOLDOWN_SECONDS = 30;
    public record Stats(BigDecimal hp, BigDecimal attack, BigDecimal defense) {
        public Stats {
            if (hp.signum() <= 0 || attack.signum() <= 0 || defense.signum() < 0)
                throw new IllegalArgumentException("Atributos inválidos");
        }
        public static Stats of(int hp, int attack, int defense) {
            return new Stats(BigDecimal.valueOf(hp), BigDecimal.valueOf(attack), BigDecimal.valueOf(defense));
        }
    }
    public record Reward(int bits, String itemCode, String itemName, int quantity) {}
    public record Floor(long number, String kind, String enemyName, String profile, Stats stats, Reward reward) {}
    public record Combat(boolean won, int rounds) {}

    public static Floor floor(long n) {
        if (n < 1) throw new IllegalArgumentException("Andar inválido");
        boolean milestone = n % 10 == 0;
        boolean boss = n % 5 == 0;
        int profile = (int) ((n - 1) % 4);
        String[] profiles = {"Equilibrado", "Ofensivo", "Defensivo", "Resistente"};
        String[][] names = {{"Koromon", "Tsunomon", "Tokomon", "Tanemon"},
                {"Agumon", "Gabumon", "Tentomon", "Gomamon"},
                {"Greymon", "Garurumon", "Kabuterimon", "Ikkakumon"},
                {"MetalGreymon", "WereGarurumon", "MegaKabuterimon", "Zudomon"},
                {"WarGreymon", "MetalGarurumon", "HerculesKabuterimon", "Vikemon"}};
        int tier = (int) Math.min(4, (n - 1) / 20);
        // Crescimento quadrático com precisão decimal; não usa exponenciais nem inteiros para atributos.
        BigDecimal scale = BigDecimal.ONE.add(BigDecimal.valueOf(n - 1).divide(BigDecimal.TEN)).pow(2);
        BigDecimal hp = scale.multiply(BigDecimal.valueOf(40));
        BigDecimal atk = scale.multiply(BigDecimal.valueOf(8));
        BigDecimal def = scale.multiply(BigDecimal.valueOf(4));
        if (profile == 1) { atk = atk.multiply(new BigDecimal("1.4")); hp = hp.multiply(new BigDecimal("0.85")); }
        if (profile == 2) { def = def.multiply(new BigDecimal("1.8")); atk = atk.multiply(new BigDecimal("0.85")); }
        if (profile == 3) { hp = hp.multiply(new BigDecimal("1.6")); atk = atk.multiply(new BigDecimal("0.85")); }
        BigDecimal multiplier = new BigDecimal(milestone ? "1.8" : boss ? "1.4" : "1");
        Stats stats = new Stats(integer(hp.multiply(multiplier)), integer(atk.multiply(multiplier)), integer(def.multiply(multiplier)));
        int bits = 100 + (int) Math.min(196, n / 10) * 25;
        if (boss) bits = Math.min(10000, bits * 2);
        Reward reward = milestone ? new Reward(bits, "ASCENSION_CORE", "Núcleo de Ascensão", 1)
                : boss ? new Reward(bits, "VIP_ENERGY_CHARGE", "Carga de Energia Total", 1)
                : new Reward(bits, "ENERGY_CELL", "Célula de Energia", 1);
        return new Floor(n, milestone ? "MARCO" : boss ? "CHEFE" : "COMUM", names[tier][profile], profiles[profile], stats, reward);
    }

    private static BigDecimal integer(BigDecimal value) { return value.setScale(0, RoundingMode.CEILING); }
    private static BigDecimal damage(Stats attacker, Stats defender) {
        return attacker.attack().subtract(defender.defense().multiply(new BigDecimal("0.5")))
                .max(attacker.attack().multiply(new BigDecimal("0.1"))).max(BigDecimal.ONE)
                .setScale(0, RoundingMode.CEILING);
    }

    /** Time ataca primeiro; guardião alterna os alvos vivos. Sem dano persistente fora da torre. */
    public static Combat fight(List<Stats> team, Stats enemy) {
        if (team.isEmpty() || team.size() > 3) throw new IllegalArgumentException("Time inválido");
        List<BigDecimal> health = new ArrayList<>(team.stream().map(Stats::hp).toList());
        BigDecimal enemyHealth = enemy.hp();
        int target = 0;
        for (int round = 1; round <= 60; round++) {
            for (int i = 0; i < team.size(); i++) {
                if (health.get(i).signum() > 0) enemyHealth = enemyHealth.subtract(damage(team.get(i), enemy));
            }
            if (enemyHealth.signum() <= 0) return new Combat(true, round);
            while (health.get(target).signum() <= 0) target = (target + 1) % team.size();
            health.set(target, health.get(target).subtract(damage(enemy, team.get(target))));
            if (health.stream().allMatch(h -> h.signum() <= 0)) return new Combat(false, round);
            target = (target + 1) % team.size();
        }
        return new Combat(false, 60);
    }
}
