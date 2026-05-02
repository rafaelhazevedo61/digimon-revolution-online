package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Attribute;
import com.dro.modules.digimon.domain.enums.Element;
import com.dro.modules.digimon.domain.enums.Species;
import com.dro.modules.digimon.domain.enums.Stage;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "digimon_infos")
@Getter
@Setter
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
}
