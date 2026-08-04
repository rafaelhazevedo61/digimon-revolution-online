package com.dro.modules.arena.infra;

import com.dro.modules.arena.domain.ArenaShopProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArenaShopProductRepository extends JpaRepository<ArenaShopProduct, String> {

    List<ArenaShopProduct> findByActiveTrueOrderByPriceCoinsAsc();
}
