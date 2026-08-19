package com.dro.modules.tutorial.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente da camada de componente de domínio do módulo de Tutorial.
 */
@Entity
@Table(name = "tutorial_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorialProgress {

    @Id
    private UUID id;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TutorialStep step;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;
}
