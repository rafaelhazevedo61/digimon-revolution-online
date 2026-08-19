package com.dro.modules.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Componente da camada de modelo de domínio do módulo de Inventário.
 */
@Entity
@Table(name = "item_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false)
    private boolean stackable;

    @Column(name = "buy_price")
    private Integer buyPrice;

    @Column(name = "sell_price")
    private Integer sellPrice;

    @Column(nullable = false)
    private boolean tradable;

    @Column(nullable = false)
    private boolean sellable;

    @Column(nullable = false)
    private boolean usable;

    @Column(name = "max_stack")
    private Integer maxStack;

    @Column(nullable = false, length = 20)
    private String rarity;

    @Column(length = 120)
    private String icon;
}
