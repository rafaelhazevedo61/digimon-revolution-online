package com.dro.modules.digitama.domain;

import com.dro.modules.digitama.domain.enums.DigitamaType;
import com.dro.modules.digitama.domain.enums.HatchSource;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "digitama_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DigitamaHistory {

    @Id
    private UUID id;

    @Column(name = "player_id", nullable = false)
    private UUID playerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "digitama_type", nullable = false)
    private DigitamaType digitamaType;

    @Column(name = "digimon_name", nullable = false)
    private String digimonName;

    @Column(name = "digimon_id", nullable = false)
    private UUID digimonId;

    @Column(name = "hatched_at", nullable = false)
    private LocalDateTime hatchedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HatchSource source;
}