package com.dro.modules.equipment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

/**
 * Componente da camada de modelo de domínio do módulo de Equipamentos.
 */
@Entity
@Table(name = "equipment_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentTemplateEntity implements Persistable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "set_code", nullable = false)
    private String setCode;

    @Column(nullable = false)
    private int tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentSlot slot;

    @Enumerated(EnumType.STRING)
    @Column
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;

    @Transient
    @Builder.Default
    private boolean newEntity = false;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }
}
