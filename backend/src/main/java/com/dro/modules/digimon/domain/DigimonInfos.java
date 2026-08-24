package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Attribute;
import com.dro.modules.digimon.domain.enums.Element;
import com.dro.modules.digimon.domain.enums.Species;
import com.dro.modules.digimon.domain.enums.Stage;
import jakarta.persistence.*;

/**
 * Componente da camada de modelo de domínio do módulo de Digimon.
 */
@Entity
@Table(name = "digimon_infos")
public class DigimonInfos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Stage stage;
    @Enumerated(EnumType.STRING)
    @Column(name = "attribute", nullable = false)
    private Attribute attribute;
    @Enumerated(EnumType.STRING)
    @Column(name = "element", nullable = false)
    private Element element;
    @Enumerated(EnumType.STRING)
    @Column(name = "specie", nullable = false)
    private Species specie;
    @Column(name = "base_hp", nullable = false)
    private int baseHp;
    @Column(name = "base_atk", nullable = false)
    private int baseAtk;
    @Column(name = "base_def", nullable = false)
    private int baseDef;
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public Species getSpecie() {
        return specie;
    }

    public void setSpecie(Species specie) {
        this.specie = specie;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public void setBaseHp(int baseHp) {
        this.baseHp = baseHp;
    }

    public int getBaseAtk() {
        return baseAtk;
    }

    public void setBaseAtk(int baseAtk) {
        this.baseAtk = baseAtk;
    }

    public int getBaseDef() {
        return baseDef;
    }

    public void setBaseDef(int baseDef) {
        this.baseDef = baseDef;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public static class DigimonInfosBuilder {
        private Long id;
        private String name;
        private Stage stage;
        private Attribute attribute;
        private Element element;
        private Species specie;
        private int baseHp;
        private int baseAtk;
        private int baseDef;
        private String imageUrl;

        DigimonInfosBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public DigimonInfos.DigimonInfosBuilder id(final Long id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigimonInfos.DigimonInfosBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigimonInfos.DigimonInfosBuilder stage(final Stage stage) {
            this.stage = stage;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigimonInfos.DigimonInfosBuilder attribute(final Attribute attribute) {
            this.attribute = attribute;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigimonInfos.DigimonInfosBuilder element(final Element element) {
            this.element = element;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigimonInfos.DigimonInfosBuilder specie(final Species specie) {
            this.specie = specie;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigimonInfos.DigimonInfosBuilder baseHp(final int baseHp) {
            this.baseHp = baseHp;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigimonInfos.DigimonInfosBuilder baseAtk(final int baseAtk) {
            this.baseAtk = baseAtk;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigimonInfos.DigimonInfosBuilder baseDef(final int baseDef) {
            this.baseDef = baseDef;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public DigimonInfos.DigimonInfosBuilder imageUrl(final String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public DigimonInfos build() {
            return new DigimonInfos(this.id, this.name, this.stage, this.attribute, this.element, this.specie, this.baseHp, this.baseAtk, this.baseDef, this.imageUrl);
        }

        @Override
        public String toString() {
            return "DigimonInfos.DigimonInfosBuilder(id=" + this.id + ", name=" + this.name + ", stage=" + this.stage + ", attribute=" + this.attribute + ", element=" + this.element + ", specie=" + this.specie + ", baseHp=" + this.baseHp + ", baseAtk=" + this.baseAtk + ", baseDef=" + this.baseDef + ", imageUrl=" + this.imageUrl + ")";
        }
    }

    public static DigimonInfos.DigimonInfosBuilder builder() {
        return new DigimonInfos.DigimonInfosBuilder();
    }

    public DigimonInfos() {
    }

    public DigimonInfos(final Long id, final String name, final Stage stage, final Attribute attribute, final Element element, final Species specie, final int baseHp, final int baseAtk, final int baseDef, final String imageUrl) {
        this.id = id;
        this.name = name;
        this.stage = stage;
        this.attribute = attribute;
        this.element = element;
        this.specie = specie;
        this.baseHp = baseHp;
        this.baseAtk = baseAtk;
        this.baseDef = baseDef;
        this.imageUrl = imageUrl;
    }
}
