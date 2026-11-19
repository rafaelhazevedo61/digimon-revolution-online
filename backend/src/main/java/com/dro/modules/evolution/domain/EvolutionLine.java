package com.dro.modules.evolution.domain;

import com.dro.modules.content.domain.AvailableContent;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente da camada de componente de domínio do módulo de Evolução.
 */
@Entity
@Table(name = "evolution_lines")
public class EvolutionLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String code;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private AvailableContent content;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "evolutionLine", fetch = FetchType.LAZY)
    private List<EvolutionLineStep> steps;

    private static List<EvolutionLineStep> $default$steps() {
        return new ArrayList<>();
    }


    public static class EvolutionLineBuilder {
        private Long id;
        private String code;
        private String name;
        private String description;
        private AvailableContent content;
        private boolean active;
        private LocalDateTime createdAt;
        private boolean steps$set;
        private List<EvolutionLineStep> steps$value;

        EvolutionLineBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public EvolutionLine.EvolutionLineBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionLine.EvolutionLineBuilder code(final String code) {
            this.code = code;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionLine.EvolutionLineBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionLine.EvolutionLineBuilder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionLine.EvolutionLineBuilder content(final AvailableContent content) {
            this.content = content;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionLine.EvolutionLineBuilder active(final boolean active) {
            this.active = active;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionLine.EvolutionLineBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public EvolutionLine.EvolutionLineBuilder steps(final List<EvolutionLineStep> steps) {
            this.steps$value = steps;
            steps$set = true;
            return this;
        }

        public EvolutionLine build() {
            List<EvolutionLineStep> steps$value = this.steps$value;
            if (!this.steps$set) steps$value = EvolutionLine.$default$steps();
            return new EvolutionLine(this.id, this.code, this.name, this.description, this.content, this.active, this.createdAt, steps$value);
        }

        @Override
        public String toString() {
            return "EvolutionLine.EvolutionLineBuilder(id=" + this.id + ", code=" + this.code + ", name=" + this.name + ", description=" + this.description + ", content=" + this.content + ", active=" + this.active + ", createdAt=" + this.createdAt + ", steps$value=" + this.steps$value + ")";
        }
    }

    public static EvolutionLine.EvolutionLineBuilder builder() {
        return new EvolutionLine.EvolutionLineBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public AvailableContent getContent() {
        return this.content;
    }

    public boolean isActive() {
        return this.active;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public List<EvolutionLineStep> getSteps() {
        return this.steps;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setContent(final AvailableContent content) {
        this.content = content;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setSteps(final List<EvolutionLineStep> steps) {
        this.steps = steps;
    }

    public EvolutionLine() {
        this.steps = EvolutionLine.$default$steps();
    }

    public EvolutionLine(final Long id, final String code, final String name, final String description, final AvailableContent content, final boolean active, final LocalDateTime createdAt, final List<EvolutionLineStep> steps) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.content = content;
        this.active = active;
        this.createdAt = createdAt;
        this.steps = steps;
    }
}
