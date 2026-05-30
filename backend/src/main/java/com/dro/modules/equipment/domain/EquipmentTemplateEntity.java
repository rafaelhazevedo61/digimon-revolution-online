package com.dro.modules.equipment.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "equipment_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentTemplateEntity {

    @Id
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentSlot slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentRarity rarity;

    @Column(name = "bonus_hp", nullable = false)
    @Builder.Default
    private int bonusHp = 0;

    @Column(name = "bonus_attack", nullable = false)
    @Builder.Default
    private int bonusAttack = 0;

    @Column(name = "bonus_defense", nullable = false)
    @Builder.Default
    private int bonusDefense = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
