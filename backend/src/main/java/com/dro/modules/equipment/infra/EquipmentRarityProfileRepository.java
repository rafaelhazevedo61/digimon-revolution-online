package com.dro.modules.equipment.infra;

import com.dro.modules.equipment.domain.EquipmentRarityProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Repositório da configuração de raridade de equipamentos. */
public interface EquipmentRarityProfileRepository extends JpaRepository<EquipmentRarityProfileEntity, Long> {

    /** Busca um perfil pela chave usada pelo sorteador. */
    Optional<EquipmentRarityProfileEntity> findByProfileKey(String profileKey);

    /** Lista os perfis em ordem estável para a tela administrativa. */
    List<EquipmentRarityProfileEntity> findAllByOrderByProfileKeyAsc();
}
