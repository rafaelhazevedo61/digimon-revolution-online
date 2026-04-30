package com.dro.modules.equipment.infra;

import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {

    List<Equipment> findByPlayerId(UUID playerId);

    List<Equipment> findByDigimonId(UUID digimonId);

    Optional<Equipment> findByDigimonIdAndSlot(UUID digimonId, EquipmentSlot slot);
}
