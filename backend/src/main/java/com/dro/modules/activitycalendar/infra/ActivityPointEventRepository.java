package com.dro.modules.activitycalendar.infra;

import com.dro.modules.activitycalendar.domain.ActivityPointEvent;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.time.LocalDate;

public interface ActivityPointEventRepository extends JpaRepository<ActivityPointEvent, UUID> {
    boolean existsByPlayerIdAndSourceAndSourceReferenceId(UUID playerId, String source, String sourceReferenceId);

    @Query("SELECT COALESCE(SUM(e.points), 0) FROM ActivityPointEvent e WHERE e.playerId = :playerId AND e.activityDate = :activityDate AND e.source = :source")
    long sumPointsByPlayerIdAndActivityDateAndSource(UUID playerId, LocalDate activityDate, String source);
}
