package com.dro.modules.loot.domain;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entrada ponderada de uma loot table.
 *
 * <p>Materiais nomeados usam {@link ItemType#EVOLUTION_MATERIAL} e o código do
 * material. Baús usam {@link ItemType#LOOT_CHEST} e o código do baú. Os demais
 * itens são identificados diretamente pelo {@link ItemType}.</p>
 */
@Entity
@Table(name = "loot_table_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LootTableEntryEntity {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 50)
    private ItemType itemType;

    @Column(name = "material_code", length = 80)
    private String materialCode;

    @Column(nullable = false)
    private int weight;

    @Column(name = "min_quantity", nullable = false)
    private int minQuantity;

    @Column(name = "max_quantity", nullable = false)
    private int maxQuantity;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
