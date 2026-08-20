package com.dro.modules.loot.domain;

import com.dro.modules.inventory.domain.ItemDefinition;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Definição de um baú temático do jogo.
 *
 * <p>O código do baú é também o código da {@link ItemDefinition} usada pelo
 * inventário para diferenciar baús negociáveis de origens distintas.</p>
 */
@Entity
@Table(name = "chest_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChestDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 120)
    private String icon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loot_table_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LootTableEntity lootTable;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_definition_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ItemDefinition itemDefinition;

    @Column(nullable = false)
    @Builder.Default
    private boolean tradable = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

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
