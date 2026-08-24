package com.dro.modules.evolution.domain;

import jakarta.persistence.*;

/**
 * Componente da camada de componente de domínio do módulo de Evolução.
 */
@Entity
@Table(name = "evolution_step_materials")
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


    public static class EvolutionStepMaterialBuilder {
        private Long id;
        private EvolutionLineStep evolutionLineStep;
        private String materialCode;
        private int quantity;
        private String description;

        EvolutionStepMaterialBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public EvolutionStepMaterial.EvolutionStepMaterialBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionStepMaterial.EvolutionStepMaterialBuilder evolutionLineStep(final EvolutionLineStep evolutionLineStep) {
            this.evolutionLineStep = evolutionLineStep;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionStepMaterial.EvolutionStepMaterialBuilder materialCode(final String materialCode) {
            this.materialCode = materialCode;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionStepMaterial.EvolutionStepMaterialBuilder quantity(final int quantity) {
            this.quantity = quantity;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionStepMaterial.EvolutionStepMaterialBuilder description(final String description) {
            this.description = description;
            return this;
        }

        public EvolutionStepMaterial build() {
            return new EvolutionStepMaterial(this.id, this.evolutionLineStep, this.materialCode, this.quantity, this.description);
        }

        @Override
        public String toString() {
            return "EvolutionStepMaterial.EvolutionStepMaterialBuilder(id=" + this.id + ", evolutionLineStep=" + this.evolutionLineStep + ", materialCode=" + this.materialCode + ", quantity=" + this.quantity + ", description=" + this.description + ")";
        }
    }

    public static EvolutionStepMaterial.EvolutionStepMaterialBuilder builder() {
        return new EvolutionStepMaterial.EvolutionStepMaterialBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public EvolutionLineStep getEvolutionLineStep() {
        return this.evolutionLineStep;
    }

    public String getMaterialCode() {
        return this.materialCode;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public String getDescription() {
        return this.description;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setEvolutionLineStep(final EvolutionLineStep evolutionLineStep) {
        this.evolutionLineStep = evolutionLineStep;
    }

    public void setMaterialCode(final String materialCode) {
        this.materialCode = materialCode;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public EvolutionStepMaterial() {
    }

    public EvolutionStepMaterial(final Long id, final EvolutionLineStep evolutionLineStep, final String materialCode, final int quantity, final String description) {
        this.id = id;
        this.evolutionLineStep = evolutionLineStep;
        this.materialCode = materialCode;
        this.quantity = quantity;
        this.description = description;
    }
}
