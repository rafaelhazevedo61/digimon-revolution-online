package com.dro.modules.boss.infra;

import com.dro.modules.boss.domain.BossDropEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BossDropRepository extends JpaRepository<BossDropEntity, Long> {

    List<BossDropEntity> findByBossId(Long bossId);
}
