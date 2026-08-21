package com.dro.modules.equipment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Configuração persistida das probabilidades de raridade de equipamentos.
 *
 * <p>Os perfis são identificados pelo contexto do sorteio, por exemplo
 * {@code BOSS_NORMAL} ou {@code BOSS_MONTHLY}. A soma dos quatro percentuais
 * é validada no banco e novamente no serviço de aplicação.</p>
 */
@Entity
@Table(name = "equipment_rarity_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentRarityProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_key", nullable = false, unique = true, length = 40)
    private String profileKey;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "common_percent", nullable = false)
    private int commonPercent;

    @Column(name = "rare_percent", nullable = false)
    private int rarePercent;

    @Column(name = "epic_percent", nullable = false)
    private int epicPercent;

    @Column(name = "legendary_percent", nullable = false)
    private int legendaryPercent;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", nullable = false, length = 80)
    @Builder.Default
    private String updatedBy = "SYSTEM";

    /** Inicializa os metadados de atualização na criação do perfil. */
    @PrePersist
    void onCreate() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (updatedBy == null) {
            updatedBy = "SYSTEM";
        }
    }

    /** Atualiza o instante da última alteração administrativa. */
    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
