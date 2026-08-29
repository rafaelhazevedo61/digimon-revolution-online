package com.dro.modules.equipment.infra;

import com.dro.modules.equipment.domain.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/** Componente da camada de repositório de persistência do módulo de Equipamentos. */
public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {
    List<Equipment> findByPlayerId(UUID playerId);
    List<Equipment> findByPlayerIdAndEquippedFalse(UUID playerId);

    long countByPlayerIdIsNull();

    long countByEquippedTrue();

    long countByEquippedFalse();
    List<Equipment> findByDigimonIdAndEquippedTrue(UUID digimonId);

    /** Compatibilidade para módulos de combate: somente equipamentos equipados afetam o Digimon. */
    @Query("SELECT equipment FROM Equipment equipment WHERE equipment.digimonId = :digimonId AND equipment.equipped = true")
    List<Equipment> findByDigimonId(@Param("digimonId") UUID digimonId);
}
