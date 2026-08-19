package com.dro.modules.boss.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * Componente da camada de componente de domínio do módulo de Boss Mundial.
 */
@Entity
@Table(name = "boss_drops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BossDropEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boss_id", nullable = false)
    @JsonIgnore
    private BossDefinitionEntity boss;

    @Column(name = "drop_type", nullable = false)
    private String dropType;

    @Column(name = "item_code")
    private String itemCode;

    @Column(name = "template_name")
    private String templateName;

    @Column(name = "equipment_rarity")
    private String equipmentRarity;

    @Column(nullable = false)
    private int chance;

    @Column(name = "min_quantity", nullable = false)
    private int minQuantity;

    @Column(name = "max_quantity", nullable = false)
    private int maxQuantity;
}
