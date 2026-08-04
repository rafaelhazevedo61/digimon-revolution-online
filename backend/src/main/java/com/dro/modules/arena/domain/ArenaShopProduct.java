package com.dro.modules.arena.domain;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "arena_shop_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArenaShopProduct {

    @Id
    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price_coins", nullable = false)
    private int priceCoins;

    @Column(name = "active", nullable = false)
    private boolean active;
}
