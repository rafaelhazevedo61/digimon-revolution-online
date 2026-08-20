package com.dro.modules.loot.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Catálogo nomeado e reutilizável de pesos de raridade e entradas de loot.
 *
 * <p>A entidade representa somente a configuração persistida. O sorteio e a
 * entrega transacional dos itens serão implementados no Sprint 2.</p>
 */
@Entity
@Table(name = "loot_tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LootTableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "min_items", nullable = false)
    @Builder.Default
    private int minItems = 1;

    @Column(name = "max_items", nullable = false)
    @Builder.Default
    private int maxItems = 4;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 80)
    @Builder.Default
    private String createdBy = "SYSTEM";

    @Column(name = "updated_by", nullable = false, length = 80)
    @Builder.Default
    private String updatedBy = "SYSTEM";

    @OneToMany(mappedBy = "lootTable", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LootTableRarityWeightEntity> rarityWeights = new ArrayList<>();

    @OneToMany(mappedBy = "lootTable", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LootTableEntryEntity> entries = new ArrayList<>();

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (createdBy == null) {
            createdBy = "SYSTEM";
        }
        if (updatedBy == null) {
            updatedBy = createdBy;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
