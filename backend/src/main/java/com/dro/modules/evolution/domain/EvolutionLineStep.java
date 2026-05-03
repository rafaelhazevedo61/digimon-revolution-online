package com.dro.modules.evolution.domain;

import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.domain.enums.Stage;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "evolution_line_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvolutionLineStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evolution_line_id", nullable = false)
    private EvolutionLine evolutionLine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "digimon_info_id", nullable = false)
    private DigimonInfos digimonInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stage stage;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "required_level", nullable = false)
    private int requiredLevel;

    @Builder.Default
    @OneToMany(mappedBy = "evolutionLineStep", fetch = FetchType.LAZY)
    private Set<EvolutionStepMaterial> materials = new HashSet<>();
}