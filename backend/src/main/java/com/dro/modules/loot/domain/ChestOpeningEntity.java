package com.dro.modules.loot.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Registro idempotente de uma abertura de baú.
 *
 * <p>A gravação completa e a entrega dos itens serão implementadas no Sprint 2;
 * a estrutura já nasce na Sprint 1 para que a chave de requisição faça parte do
 * desenho relacional desde o começo.</p>
 */
@Entity
@Table(name = "chest_openings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChestOpeningEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, unique = true, length = 120)
    private String requestId;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chest_definition_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ChestDefinitionEntity chestDefinition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LootRarity rarity;

    @Column(nullable = false, length = 120)
    private String source;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @OneToMany(mappedBy = "chestOpening", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChestOpeningItemEntity> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (openedAt == null) {
            openedAt = LocalDateTime.now();
        }
    }
}
