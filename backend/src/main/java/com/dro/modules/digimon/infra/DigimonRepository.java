package com.dro.modules.digimon.infra;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DigimonRepository extends JpaRepository<Digimon, UUID> {

    List<Digimon> findByPlayerId(UUID playerId);

    Page<Digimon> findByStatusOrderByLevelDescExperienceDesc(DigimonStatus status, Pageable pageable);

    Page<Digimon> findByStatusOrderByGradeAscLevelDesc(DigimonStatus status, Pageable pageable);

    Page<Digimon> findByStatusOrderByRebirthCountDescLevelDesc(DigimonStatus status, Pageable pageable);

}
