package com.dro.modules.loot.domain;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Item e quantidade registrados como resultado de uma abertura de baú.
 */
@Entity
@Table(name = "chest_opening_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChestOpeningItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chest_opening_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ChestOpeningEntity chestOpening;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LootRarity rarity;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private ItemType itemType;

    @Column(name = "material_code", length = 80)
    private String materialCode;

    @Column(nullable = false)
    private int quantity;
}
