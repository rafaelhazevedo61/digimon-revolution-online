package com.dro.modules.mission.domain;

import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.loot.domain.LootRarity;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Componente da camada de componente de domínio do módulo de Missões.
 */
@Entity
@Table(name = "mission_loot_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionLootItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    @JsonIgnore
    private MissionDefinitionEntity mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LootRarity rarity;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Column(nullable = false)
    private int quantity;
}
