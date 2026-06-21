package com.dro.modules.equipment.infra;

import com.dro.modules.equipment.domain.EquipmentTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentTemplateRepository extends JpaRepository<EquipmentTemplateEntity, Long> {

    List<EquipmentTemplateEntity> findByActiveTrueOrderByNameAsc();

    List<EquipmentTemplateEntity> findAllByOrderByNameAsc();

    java.util.Optional<EquipmentTemplateEntity> findBySetCodeAndSlotAndTier(String setCode, com.dro.modules.equipment.domain.EquipmentSlot slot, int tier);

    java.util.Optional<EquipmentTemplateEntity> findByName(String name);
}
