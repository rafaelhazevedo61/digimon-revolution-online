package com.dro.modules.equipment.infra;

import com.dro.modules.equipment.domain.EquipmentTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentTemplateRepository extends JpaRepository<EquipmentTemplateEntity, String> {

    List<EquipmentTemplateEntity> findByActiveTrueOrderByNameAsc();

    List<EquipmentTemplateEntity> findAllByOrderByNameAsc();
}
