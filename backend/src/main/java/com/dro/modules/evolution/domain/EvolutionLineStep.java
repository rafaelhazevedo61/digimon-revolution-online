package com.dro.modules.evolution.domain;

import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.domain.enums.Stage;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Componente da camada de componente de domínio do módulo de Evolução.
 */
@Entity
@Table(name = "evolution_line_steps")
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
    @OneToMany(mappedBy = "evolutionLineStep", fetch = FetchType.LAZY)
    private Set<EvolutionStepMaterial> materials;

    private static Set<EvolutionStepMaterial> $default$materials() {
        return new HashSet<>();
    }


    public static class EvolutionLineStepBuilder {
        private Long id;
        private EvolutionLine evolutionLine;
        private DigimonInfos digimonInfo;
        private Stage stage;
        private int stepOrder;
        private int requiredLevel;
        private boolean materials$set;
        private Set<EvolutionStepMaterial> materials$value;

        EvolutionLineStepBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public EvolutionLineStep.EvolutionLineStepBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionLineStep.EvolutionLineStepBuilder evolutionLine(final EvolutionLine evolutionLine) {
            this.evolutionLine = evolutionLine;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionLineStep.EvolutionLineStepBuilder digimonInfo(final DigimonInfos digimonInfo) {
            this.digimonInfo = digimonInfo;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionLineStep.EvolutionLineStepBuilder stage(final Stage stage) {
            this.stage = stage;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionLineStep.EvolutionLineStepBuilder stepOrder(final int stepOrder) {
            this.stepOrder = stepOrder;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionLineStep.EvolutionLineStepBuilder requiredLevel(final int requiredLevel) {
            this.requiredLevel = requiredLevel;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionLineStep.EvolutionLineStepBuilder materials(final Set<EvolutionStepMaterial> materials) {
            this.materials$value = materials;
            materials$set = true;
            return this;
        }

        public EvolutionLineStep build() {
            Set<EvolutionStepMaterial> materials$value = this.materials$value;
            if (!this.materials$set) materials$value = EvolutionLineStep.$default$materials();
            return new EvolutionLineStep(this.id, this.evolutionLine, this.digimonInfo, this.stage, this.stepOrder, this.requiredLevel, materials$value);
        }

        @Override
        public String toString() {
            return "EvolutionLineStep.EvolutionLineStepBuilder(id=" + this.id + ", evolutionLine=" + this.evolutionLine + ", digimonInfo=" + this.digimonInfo + ", stage=" + this.stage + ", stepOrder=" + this.stepOrder + ", requiredLevel=" + this.requiredLevel + ", materials$value=" + this.materials$value + ")";
        }
    }

    public static EvolutionLineStep.EvolutionLineStepBuilder builder() {
        return new EvolutionLineStep.EvolutionLineStepBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public EvolutionLine getEvolutionLine() {
        return this.evolutionLine;
    }

    public DigimonInfos getDigimonInfo() {
        return this.digimonInfo;
    }

    public Stage getStage() {
        return this.stage;
    }

    public int getStepOrder() {
        return this.stepOrder;
    }

    public int getRequiredLevel() {
        return this.requiredLevel;
    }

    public Set<EvolutionStepMaterial> getMaterials() {
        return this.materials;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setEvolutionLine(final EvolutionLine evolutionLine) {
        this.evolutionLine = evolutionLine;
    }

    public void setDigimonInfo(final DigimonInfos digimonInfo) {
        this.digimonInfo = digimonInfo;
    }

    public void setStage(final Stage stage) {
        this.stage = stage;
    }

    public void setStepOrder(final int stepOrder) {
        this.stepOrder = stepOrder;
    }

    public void setRequiredLevel(final int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public void setMaterials(final Set<EvolutionStepMaterial> materials) {
        this.materials = materials;
    }

    public EvolutionLineStep() {
        this.materials = EvolutionLineStep.$default$materials();
    }

    public EvolutionLineStep(final Long id, final EvolutionLine evolutionLine, final DigimonInfos digimonInfo, final Stage stage, final int stepOrder, final int requiredLevel, final Set<EvolutionStepMaterial> materials) {
        this.id = id;
        this.evolutionLine = evolutionLine;
        this.digimonInfo = digimonInfo;
        this.stage = stage;
        this.stepOrder = stepOrder;
        this.requiredLevel = requiredLevel;
        this.materials = materials;
    }
}
