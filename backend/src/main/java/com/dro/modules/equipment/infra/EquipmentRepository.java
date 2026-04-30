package com.dro.modules.equipment.infra;

import com.dro.modules.equipment.domain.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {

    List<Equipment> findByDigimonId(UUID digimonId);
}
