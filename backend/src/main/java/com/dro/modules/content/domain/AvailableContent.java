package com.dro.modules.content.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Componente da camada de componente de domínio do módulo de Conteúdo.
 */
@Entity
@Table(name = "available_contents")
public class AvailableContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String code;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "release_order", nullable = false)
    private int releaseOrder;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    public static class AvailableContentBuilder {
        private Long id;
        private String code;
        private String name;
        private String description;
        private boolean active;
        private int releaseOrder;
        private LocalDateTime createdAt;

        AvailableContentBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public AvailableContent.AvailableContentBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AvailableContent.AvailableContentBuilder code(final String code) {
            this.code = code;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AvailableContent.AvailableContentBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AvailableContent.AvailableContentBuilder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AvailableContent.AvailableContentBuilder active(final boolean active) {
            this.active = active;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AvailableContent.AvailableContentBuilder releaseOrder(final int releaseOrder) {
            this.releaseOrder = releaseOrder;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AvailableContent.AvailableContentBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AvailableContent build() {
            return new AvailableContent(this.id, this.code, this.name, this.description, this.active, this.releaseOrder, this.createdAt);
        }

        @Override
        public String toString() {
            return "AvailableContent.AvailableContentBuilder(id=" + this.id + ", code=" + this.code + ", name=" + this.name + ", description=" + this.description + ", active=" + this.active + ", releaseOrder=" + this.releaseOrder + ", createdAt=" + this.createdAt + ")";
        }
    }

    public static AvailableContent.AvailableContentBuilder builder() {
        return new AvailableContent.AvailableContentBuilder();
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

    public boolean isActive() {
        return this.active;
    }

    public int getReleaseOrder() {
        return this.releaseOrder;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
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

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setReleaseOrder(final int releaseOrder) {
        this.releaseOrder = releaseOrder;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public AvailableContent() {
    }

    public AvailableContent(final Long id, final String code, final String name, final String description, final boolean active, final int releaseOrder, final LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.active = active;
        this.releaseOrder = releaseOrder;
        this.createdAt = createdAt;
    }
}
