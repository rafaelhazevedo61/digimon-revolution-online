package com.dro.modules.inventory.infra;

import com.dro.modules.inventory.domain.ItemDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemDefinitionRepository extends JpaRepository<ItemDefinition, Long> {

    Optional<ItemDefinition> findByCode(String code);
}
