package com.dro.modules.tutorial.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tutorial_completions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorialCompletion {

    @Id
    private UUID playerId;

    @Column(name = "finished_at", nullable = false)
    private LocalDateTime finishedAt;
}
