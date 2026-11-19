package com.dro.modules.digitama.domain;

import com.dro.modules.digimon.domain.DigimonInfos;
import jakarta.persistence.*;

/**
 * Componente da camada de componente de domínio do módulo de Digitama.
 */
@Entity
@Table(name = "digitama_pool_entries")
public class DigitamaPoolEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "digitama_pool_id", nullable = false)
    private DigitamaPool digitamaPool;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "digimon_info_id", nullable = false)
    private DigimonInfos digimonInfo;
    @Column(nullable = false)
    private int weight;
    @Column(nullable = false)
    private boolean active;


    public static class DigitamaPoolEntryBuilder {
        private Long id;
        private DigitamaPool digitamaPool;
        private DigimonInfos digimonInfo;
        private int weight;
        private boolean active;

        DigitamaPoolEntryBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public DigitamaPoolEntry.DigitamaPoolEntryBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaPoolEntry.DigitamaPoolEntryBuilder digitamaPool(final DigitamaPool digitamaPool) {
            this.digitamaPool = digitamaPool;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaPoolEntry.DigitamaPoolEntryBuilder digimonInfo(final DigimonInfos digimonInfo) {
            this.digimonInfo = digimonInfo;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaPoolEntry.DigitamaPoolEntryBuilder weight(final int weight) {
            this.weight = weight;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigitamaPoolEntry.DigitamaPoolEntryBuilder active(final boolean active) {
            this.active = active;
            return this;
        }

        public DigitamaPoolEntry build() {
            return new DigitamaPoolEntry(this.id, this.digitamaPool, this.digimonInfo, this.weight, this.active);
        }

        @Override
        public String toString() {
            return "DigitamaPoolEntry.DigitamaPoolEntryBuilder(id=" + this.id + ", digitamaPool=" + this.digitamaPool + ", digimonInfo=" + this.digimonInfo + ", weight=" + this.weight + ", active=" + this.active + ")";
        }
    }

    public static DigitamaPoolEntry.DigitamaPoolEntryBuilder builder() {
        return new DigitamaPoolEntry.DigitamaPoolEntryBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public DigitamaPool getDigitamaPool() {
        return this.digitamaPool;
    }

    public DigimonInfos getDigimonInfo() {
        return this.digimonInfo;
    }

    public int getWeight() {
        return this.weight;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setDigitamaPool(final DigitamaPool digitamaPool) {
        this.digitamaPool = digitamaPool;
    }

    public void setDigimonInfo(final DigimonInfos digimonInfo) {
        this.digimonInfo = digimonInfo;
    }

    public void setWeight(final int weight) {
        this.weight = weight;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public DigitamaPoolEntry() {
    }

    public DigitamaPoolEntry(final Long id, final DigitamaPool digitamaPool, final DigimonInfos digimonInfo, final int weight, final boolean active) {
        this.id = id;
        this.digitamaPool = digitamaPool;
        this.digimonInfo = digimonInfo;
        this.weight = weight;
        this.active = active;
    }
}
