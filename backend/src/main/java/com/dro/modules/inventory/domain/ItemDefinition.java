package com.dro.modules.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

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
}
