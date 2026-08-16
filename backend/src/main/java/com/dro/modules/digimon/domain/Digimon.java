package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.*;
import com.dro.shared.exception.UnprocessableException;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DigimonGrade grade;

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

    @Column(name = "arena_rating", nullable = false)
    @Builder.Default
    private int arenaRating = 1000;

    @Column(name = "arena_wins", nullable = false)
    @Builder.Default
    private int arenaWins = 0;

    @Column(name = "arena_losses", nullable = false)
    @Builder.Default
    private int arenaLosses = 0;

    @Column(name = "is_bot", nullable = false)
    @Builder.Default
    private boolean bot = false;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private long version = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DigimonStatus status;

    @Column(name = "reborned_from")
    private UUID rebornedFrom;

    @Enumerated(EnumType.STRING)
    @Column(name = "trait")
    private Trait trait;

    @Column(name = "weapon_id")
    private UUID weaponId;

    @Column(name = "armor_id")
    private UUID armorId;

    @Column(name = "accessory_id")
    private UUID accessoryId;

    @Column(name = "digimon_info_id")
    private Long digimonInfoId;

    public void equipWeapon(UUID equipmentId) { this.weaponId = equipmentId; }
    public void unequipWeapon() { this.weaponId = null; }

    public void equipArmor(UUID equipmentId) { this.armorId = equipmentId; }
    public void unequipArmor() { this.armorId = null; }

    public void equipAccessory(UUID equipmentId) { this.accessoryId = equipmentId; }
    public void unequipAccessory() { this.accessoryId = null; }

    public UUID getEquipmentIdBySlot(com.dro.modules.equipment.domain.EquipmentSlot slot) {
        return switch (slot) {
            case WEAPON -> weaponId;
            case ARMOR -> armorId;
            case ACCESSORY -> accessoryId;
        };
    }

    public void setEquipmentBySlot(com.dro.modules.equipment.domain.EquipmentSlot slot, UUID equipmentId) {
        switch (slot) {
            case WEAPON -> this.weaponId = equipmentId;
            case ARMOR -> this.armorId = equipmentId;
            case ACCESSORY -> this.accessoryId = equipmentId;
        }
    }

    public void clearSlot(com.dro.modules.equipment.domain.EquipmentSlot slot) {
        setEquipmentBySlot(slot, null);
    }

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
        regenerateEnergy(0);
    }

    public void regenerateEnergy(int maxEnergyBonus) {

        int effectiveMax = maxEnergy + maxEnergyBonus;

        if (energy >= effectiveMax) return;

        Instant now = Instant.now();

        long minutesPassed = Duration.between(lastEnergyUpdate, now).toMinutes();

        long energyRecovered = minutesPassed / 5;

        if (energyRecovered > 0) {

            energy = (int) Math.min(effectiveMax, energy + energyRecovered);

            lastEnergyUpdate = lastEnergyUpdate.plus(
                    Duration.ofMinutes(energyRecovered * 5)
            );
        }
    }

    public void consumeEnergy(int amount) {

        if (energy < amount) {
            throw new UnprocessableException("Energia insuficiente");
        }

        energy -= amount;
    }
}