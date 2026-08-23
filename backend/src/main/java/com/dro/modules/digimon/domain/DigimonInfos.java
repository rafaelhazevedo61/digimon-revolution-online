package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Attribute;
import com.dro.modules.digimon.domain.enums.Element;
import com.dro.modules.digimon.domain.enums.Species;
import com.dro.modules.digimon.domain.enums.Stage;
import jakarta.persistence.*;
import lombok.*;

/**
 * Componente da camada de modelo de domínio do módulo de Digimon.
 */
@Entity
@Table(name = "digimon_infos")
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    public Long getId () {
        return id;
    }

    public void setId (Long id) {
        this.id = id;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public Stage getStage () {
        return stage;
    }

    public void setStage (Stage stage) {
        this.stage = stage;
    }

    public Attribute getAttribute () {
        return attribute;
    }

    public void setAttribute (Attribute attribute) {
        this.attribute = attribute;
    }

    public Element getElement () {
        return element;
    }

    public void setElement (Element element) {
        this.element = element;
    }

    public Species getSpecie () {
        return specie;
    }

    public void setSpecie (Species specie) {
        this.specie = specie;
    }

    public int getBaseHp () {
        return baseHp;
    }

    public void setBaseHp (int baseHp) {
        this.baseHp = baseHp;
    }

    public int getBaseAtk () {
        return baseAtk;
    }

    public void setBaseAtk (int baseAtk) {
        this.baseAtk = baseAtk;
    }

    public int getBaseDef () {
        return baseDef;
    }

    public void setBaseDef (int baseDef) {
        this.baseDef = baseDef;
    }

    public String getImageUrl () {
        return imageUrl;
    }

    public void setImageUrl (String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
