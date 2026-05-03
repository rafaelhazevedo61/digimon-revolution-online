package com.dro.modules.evolution.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evolution_step_materials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvolutionStepMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evolution_line_step_id", nullable = false)
    private EvolutionLineStep evolutionLineStep;

    @Column(name = "material_code", nullable = false, length = 80)
    private String materialCode;

    @Column(nullable = false)
    private int quantity;

    @Column(length = 255)
    private String description;
}
