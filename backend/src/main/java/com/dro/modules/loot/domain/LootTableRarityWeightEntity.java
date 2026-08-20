package com.dro.modules.loot.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * Peso de uma raridade dentro de uma loot table.
 */
@Entity
@Table(
        name = "loot_table_rarity_weights",
        uniqueConstraints = @UniqueConstraint(columnNames = {"loot_table_id", "rarity"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LootTableRarityWeightEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loot_table_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LootTableEntity lootTable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LootRarity rarity;

    @Column(nullable = false)
    private int weight;
}
