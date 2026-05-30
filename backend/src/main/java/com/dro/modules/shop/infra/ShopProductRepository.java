package com.dro.modules.shop.infra;

import com.dro.modules.shop.domain.ShopProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopProductRepository extends JpaRepository<ShopProductEntity, String> {

    List<ShopProductEntity> findByActiveTrue();

    Optional<ShopProductEntity> findByEquipmentTemplateNameIgnoreCase(String equipmentTemplateName);
}
