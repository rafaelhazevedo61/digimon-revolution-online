package com.dro.modules.boss.infra;

import com.dro.modules.boss.domain.BossDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BossDefinitionRepository extends JpaRepository<BossDefinitionEntity, Long> {

    Optional<BossDefinitionEntity> findByCode(String code);

    @Query("SELECT b FROM BossDefinitionEntity b WHERE b.active = true ORDER BY b.requiredLevel ASC, b.id ASC")
    List<BossDefinitionEntity> findAllActive();

    List<BossDefinitionEntity> findAllByOrderByIdAsc();
}
