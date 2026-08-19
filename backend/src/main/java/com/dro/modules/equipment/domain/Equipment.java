package com.dro.modules.equipment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Peça de equipamento individual pertencente a um Digimon.
 *
 * <p>Diferentemente de itens empilháveis, cada equipamento mantém identidade,
 * slot, raridade, tier, set e nível de refinamento próprios. O bônus efetivo
 * combina o valor base com o multiplicador de raridade e o refinamento.</p>
 */
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

    /** Marca a peça como ocupando seu slot no Digimon. */
    public void equip() {
        this.equipped = true;
    }

    /** Libera a peça do slot atualmente ocupado. */
    public void unequip() {
        this.equipped = false;
    }

    /** Calcula o bônus efetivo de HP após raridade e refinamento. */
    public int getEffectiveBonusHp() {
        if (bonusHp <= 0) return 0;
        return (int) Math.round(bonusHp * rarity.getStatMultiplier()) + (refinementLevel * 2);
    }

    /** Calcula o bônus efetivo de ATK após raridade e refinamento. */
    public int getEffectiveBonusAttack() {
        if (bonusAttack <= 0) return 0;
        return (int) Math.round(bonusAttack * rarity.getStatMultiplier()) + (refinementLevel * 2);
    }

    /** Calcula o bônus efetivo de DEF após raridade e refinamento. */
    public int getEffectiveBonusDefense() {
        if (bonusDefense <= 0) return 0;
        return (int) Math.round(bonusDefense * rarity.getStatMultiplier()) + (refinementLevel * 2);
    }
}
