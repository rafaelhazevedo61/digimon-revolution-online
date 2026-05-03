package com.dro.modules.digitama.domain;

import com.dro.modules.digimon.domain.DigimonInfos;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "digitama_pool_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}