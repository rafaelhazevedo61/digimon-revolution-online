package com.dro.modules.digimon.domain;

import jakarta.persistence.*;
import lombok.*;

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

    public void gainExperience(int baseXp) {

        double rarityMultiplier = RarityRules.getXpMultiplier(this.rarity);
        double personalityMultiplier =
                PersonalityRules.getXpMultiplier(this.personality);

        int finalXp = (int) Math.floor(
                baseXp
                        * rarityMultiplier
                        * personalityMultiplier
        );


        this.experience += finalXp;

        // Permite múltiplos level-ups
        while (true) {

            int xpRequired = xpToNextLevel();

            if (this.experience < xpRequired) {
                break;
            }

            this.experience -= xpRequired;
            this.level++;
        }
    }

    private int xpToNextLevel() {
        return this.level * 100;
    }

    private void levelUp() {
        this.level++;
        this.hp += 2;
        this.attack += 1;
        this.defense += 1;
    }
}
