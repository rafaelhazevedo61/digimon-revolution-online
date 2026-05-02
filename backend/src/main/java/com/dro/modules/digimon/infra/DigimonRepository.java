package com.dro.modules.digimon.infra;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DigimonRepository extends JpaRepository<Digimon, UUID> {

    List<Digimon> findByPlayerId(UUID playerId);

    Page<Digimon> findByStatusOrderByLevelDescExperienceDesc(DigimonStatus status, Pageable pageable);

    @Query("SELECT d FROM Digimon d WHERE d.status = :status ORDER BY " +
            "CASE d.grade WHEN 'SSS' THEN 0 WHEN 'SS' THEN 1 WHEN 'S' THEN 2 " +
            "WHEN 'A' THEN 3 WHEN 'B' THEN 4 WHEN 'C' THEN 5 WHEN 'D' THEN 6 WHEN 'E' THEN 7 END ASC, " +
            "d.level DESC")
    Page<Digimon> findByStatusOrderByGradeQualityAscLevelDesc(@Param("status") DigimonStatus status, Pageable pageable);

    Page<Digimon> findByStatusOrderByRebirthCountDescLevelDesc(DigimonStatus status, Pageable pageable);

}
