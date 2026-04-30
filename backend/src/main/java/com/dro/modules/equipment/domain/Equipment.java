package com.dro.modules.equipment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "equipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID playerId;

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

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public boolean isEquipped() {
        return digimonId != null;
    }

    public void equip(UUID digimonId) {
        this.digimonId = digimonId;
    }

    public void unequip() {
        this.digimonId = null;
    }
}
