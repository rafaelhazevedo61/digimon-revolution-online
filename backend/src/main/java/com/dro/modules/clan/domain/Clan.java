package com.dro.modules.clan.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Clãs.
 */
@Entity
@Table(name = "clans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clan {

    @Id
    private UUID id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 5)
    private String tag;

    @Column(length = 280)
    private String description;

    @Column(name = "leader_id", nullable = false)
    private UUID leaderId;

    @Column(length = 50)
    private String emblem;

    @Column(name = "max_members", nullable = false)
    @Builder.Default
    private int maxMembers = 5;

    @Column(nullable = false)
    @Builder.Default
    private int level = 1;

    @Column(nullable = false)
    @Builder.Default
    private int experience = 0;

    @Column(name = "honor_marks", nullable = false)
    @Builder.Default
    private int honorMarks = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "dissolved_at")
    private LocalDateTime dissolvedAt;
}
