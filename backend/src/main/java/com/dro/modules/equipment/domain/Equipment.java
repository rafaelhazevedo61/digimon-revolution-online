package com.dro.modules.equipment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_equipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID digimonId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentSlot slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentRarity rarity;

    @Column(nullable = false)
    private int bonusHp;

    @Column(nullable = false)
    private int bonusAttack;

    @Column(nullable = false)
    private int bonusDefense;

    @Column(name = "set_code")
    private String setCode;

    @Column
    private int tier;

    @Column(name = "refinement_level", nullable = false)
    @Builder.Default
    private int refinementLevel = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean equipped = false;

    public void equip() {
        this.equipped = true;
    }

    public void unequip() {
        this.equipped = false;
    }

    public int getEffectiveBonusHp() {
        return (int) Math.round(bonusHp * rarity.getStatMultiplier()) + (refinementLevel * 2);
    }

    public int getEffectiveBonusAttack() {
        return (int) Math.round(bonusAttack * rarity.getStatMultiplier()) + (refinementLevel * 2);
    }

    public int getEffectiveBonusDefense() {
        return (int) Math.round(bonusDefense * rarity.getStatMultiplier()) + (refinementLevel * 2);
    }
}
