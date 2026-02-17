package com.dro.modules.digimon.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "digimons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Digimon {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID playerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String stage;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private int experience;

    private int hp;
    private int attack;
    private int defense;

    private int ivHp;
    private int ivAttack;
    private int ivDefense;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
