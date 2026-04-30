package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "digimons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Digimon {

    private static final int MAX_LEVEL = DigimonLevelRules.MAX_LEVEL;

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID playerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stage stage;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private int experience;

    private int hp;
    private int attack;
    private int defense;

    private int ivHp;
    private int ivAttack;
    private int ivDefense;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rarity rarity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Personality personality;

    @Column(nullable = false)
    private int energy;

    @Column(name = "max_energy", nullable = false)
    private int maxEnergy;

    @Column(name = "last_energy_update", nullable = false)
    private Instant lastEnergyUpdate;

    @Column(nullable = false)
    private int bits;

    @Column(name = "rebirth_count", nullable = false)
    private int rebirthCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DigimonStatus status;

    @Column(name = "reborned_from")
    private UUID rebornedFrom;

    @Enumerated(EnumType.STRING)
    @Column(name = "trait")
    private Trait trait;

    public void gainExperience(int baseXp) {

        double rarityMultiplier = RarityRules.getXpMultiplier(this.rarity);
        double personalityMultiplier =
                PersonalityRules.getXpMultiplier(this.personality);
        double traitMultiplier = TraitRules.getXpMultiplier(this.trait);

        int finalXp = (int) Math.floor(
                baseXp
                        * rarityMultiplier
                        * personalityMultiplier
                        * traitMultiplier
        );

        this.experience += finalXp;

        while (this.level < MAX_LEVEL) {

            int xpRequired = xpToNextLevel();

            if (this.experience < xpRequired) {
                break;
            }

            this.experience -= xpRequired;
            levelUp();
        }

        if (this.level > MAX_LEVEL) {
            this.level = MAX_LEVEL;
        }
    }

    private int xpToNextLevel() {
        return DigimonLevelRules.xpToNextLevel(this.level);
    }
    private void levelUp() {

        if (this.level >= MAX_LEVEL) {
            this.level = MAX_LEVEL;
            return;
        }

        this.level++;
        this.hp += 2;
        this.attack += 1;
        this.defense += 1;
    }

    public void regenerateEnergy() {

        if (energy >= maxEnergy) return;

        Instant now = Instant.now();

        long minutesPassed = Duration.between(lastEnergyUpdate, now).toMinutes();

        long energyRecovered = minutesPassed / 5;

        if (energyRecovered > 0) {

            energy = (int) Math.min(maxEnergy, energy + energyRecovered);

            lastEnergyUpdate = lastEnergyUpdate.plus(
                    Duration.ofMinutes(energyRecovered * 5)
            );
        }
    }

    public void consumeEnergy(int amount) {

        if (energy < amount) {
            throw new RuntimeException("Energia insuficiente");
        }

        energy -= amount;
    }
}