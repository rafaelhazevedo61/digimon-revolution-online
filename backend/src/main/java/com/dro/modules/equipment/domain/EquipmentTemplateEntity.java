package com.dro.modules.equipment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "equipment_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentTemplateEntity implements Persistable<String> {

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

    @Transient
    @Builder.Default
    private boolean newEntity = false;

    @Override
    public String getId() {
        return name;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }
}
