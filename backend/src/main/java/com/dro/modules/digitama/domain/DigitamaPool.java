package com.dro.modules.digitama.domain;

import com.dro.modules.content.domain.AvailableContent;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente da camada de componente de domínio do módulo de Digitama.
 */
@Entity
@Table(name = "digitama_pools")
public class DigitamaPool {
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
    @OneToMany(mappedBy = "digitamaPool", fetch = FetchType.LAZY)
    private List<DigitamaPoolEntry> entries;

    private static List<DigitamaPoolEntry> $default$entries() {
        return new ArrayList<>();
    }


    public static class DigitamaPoolBuilder {
        private Long id;
        private String code;
        private String name;
        private String description;
        private AvailableContent content;
        private boolean active;
        private LocalDateTime createdAt;
        private boolean entries$set;
        private List<DigitamaPoolEntry> entries$value;

        DigitamaPoolBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public DigitamaPool.DigitamaPoolBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaPool.DigitamaPoolBuilder code(final String code) {
            this.code = code;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaPool.DigitamaPoolBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaPool.DigitamaPoolBuilder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaPool.DigitamaPoolBuilder content(final AvailableContent content) {
            this.content = content;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaPool.DigitamaPoolBuilder active(final boolean active) {
            this.active = active;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaPool.DigitamaPoolBuilder createdAt(final LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaPool.DigitamaPoolBuilder entries(final List<DigitamaPoolEntry> entries) {
            this.entries$value = entries;
            entries$set = true;
            return this;
        }

        public DigitamaPool build() {
            List<DigitamaPoolEntry> entries$value = this.entries$value;
            if (!this.entries$set) entries$value = DigitamaPool.$default$entries();
            return new DigitamaPool(this.id, this.code, this.name, this.description, this.content, this.active, this.createdAt, entries$value);
        }

        @Override
        public String toString() {
            return "DigitamaPool.DigitamaPoolBuilder(id=" + this.id + ", code=" + this.code + ", name=" + this.name + ", description=" + this.description + ", content=" + this.content + ", active=" + this.active + ", createdAt=" + this.createdAt + ", entries$value=" + this.entries$value + ")";
        }
    }

    public static DigitamaPool.DigitamaPoolBuilder builder() {
        return new DigitamaPool.DigitamaPoolBuilder();
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

    public List<DigitamaPoolEntry> getEntries() {
        return this.entries;
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

    public void setEntries(final List<DigitamaPoolEntry> entries) {
        this.entries = entries;
    }

    public DigitamaPool() {
        this.entries = DigitamaPool.$default$entries();
    }

    public DigitamaPool(final Long id, final String code, final String name, final String description, final AvailableContent content, final boolean active, final LocalDateTime createdAt, final List<DigitamaPoolEntry> entries) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.content = content;
        this.active = active;
        this.createdAt = createdAt;
        this.entries = entries;
    }
}
