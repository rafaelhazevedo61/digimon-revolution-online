package com.dro.modules.loot.domain;

import jakarta.persistence.*;
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


    private ChestDefinitionEntity chestDefinition;
    @Column(nullable = false)
    private int quantity;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LootRarity rarity;
    @Column(nullable = false, length = 120)
    private String source;
    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;
    @OneToMany(mappedBy = "chestOpening", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChestOpeningItemEntity> items;

    @PrePersist
    void onCreate() {
        if (openedAt == null) {
            openedAt = LocalDateTime.now();
        }
    }

    private static List<ChestOpeningItemEntity> $default$items() {
        return new ArrayList<>();
    }


    public static class ChestOpeningEntityBuilder {
        private Long id;
        private String requestId;
        private UUID playerId;
        private ChestDefinitionEntity chestDefinition;
        private int quantity = 1;
        private LootRarity rarity;
        private String source;
        private LocalDateTime openedAt;
        private boolean items$set;
        private List<ChestOpeningItemEntity> items$value;

        ChestOpeningEntityBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ChestOpeningEntity.ChestOpeningEntityBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestOpeningEntity.ChestOpeningEntityBuilder requestId(final String requestId) {
            this.requestId = requestId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestOpeningEntity.ChestOpeningEntityBuilder playerId(final UUID playerId) {
            this.playerId = playerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestOpeningEntity.ChestOpeningEntityBuilder chestDefinition(final ChestDefinitionEntity chestDefinition) {
            this.chestDefinition = chestDefinition;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestOpeningEntity.ChestOpeningEntityBuilder quantity(final int quantity) {
            this.quantity = quantity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestOpeningEntity.ChestOpeningEntityBuilder rarity(final LootRarity rarity) {
            this.rarity = rarity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestOpeningEntity.ChestOpeningEntityBuilder source(final String source) {
            this.source = source;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestOpeningEntity.ChestOpeningEntityBuilder openedAt(final LocalDateTime openedAt) {
            this.openedAt = openedAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ChestOpeningEntity.ChestOpeningEntityBuilder items(final List<ChestOpeningItemEntity> items) {
            this.items$value = items;
            items$set = true;
            return this;
        }

        public ChestOpeningEntity build() {
            List<ChestOpeningItemEntity> items$value = this.items$value;
            if (!this.items$set) items$value = ChestOpeningEntity.$default$items();
            return new ChestOpeningEntity(this.id, this.requestId, this.playerId, this.chestDefinition, this.quantity, this.rarity, this.source, this.openedAt, items$value);
        }

        @Override
        public String toString() {
            return "ChestOpeningEntity.ChestOpeningEntityBuilder(id=" + this.id + ", requestId=" + this.requestId + ", playerId=" + this.playerId + ", chestDefinition=" + this.chestDefinition + ", quantity=" + this.quantity + ", rarity=" + this.rarity + ", source=" + this.source + ", openedAt=" + this.openedAt + ", items$value=" + this.items$value + ")";
        }
    }

    public static ChestOpeningEntity.ChestOpeningEntityBuilder builder() {
        return new ChestOpeningEntity.ChestOpeningEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    public ChestDefinitionEntity getChestDefinition() {
        return this.chestDefinition;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public LootRarity getRarity() {
        return this.rarity;
    }

    public String getSource() {
        return this.source;
    }

    public LocalDateTime getOpenedAt() {
        return this.openedAt;
    }

    public List<ChestOpeningItemEntity> getItems() {
        return this.items;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setRequestId(final String requestId) {
        this.requestId = requestId;
    }

    public void setPlayerId(final UUID playerId) {
        this.playerId = playerId;
    }

    public void setChestDefinition(final ChestDefinitionEntity chestDefinition) {
        this.chestDefinition = chestDefinition;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public void setRarity(final LootRarity rarity) {
        this.rarity = rarity;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public void setOpenedAt(final LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }

    public void setItems(final List<ChestOpeningItemEntity> items) {
        this.items = items;
    }

    public ChestOpeningEntity() {
        this.quantity = 1;
        this.items = ChestOpeningEntity.$default$items();
    }

    public ChestOpeningEntity(final Long id, final String requestId, final UUID playerId, final ChestDefinitionEntity chestDefinition, final int quantity, final LootRarity rarity, final String source, final LocalDateTime openedAt, final List<ChestOpeningItemEntity> items) {
        this.id = id;
        this.requestId = requestId;
        this.playerId = playerId;
        this.chestDefinition = chestDefinition;
        this.quantity = quantity;
        this.rarity = rarity;
        this.source = source;
        this.openedAt = openedAt;
        this.items = items;
    }
}
