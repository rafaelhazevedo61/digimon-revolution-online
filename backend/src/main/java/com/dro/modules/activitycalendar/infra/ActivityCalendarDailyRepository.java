package com.dro.modules.activitycalendar.infra;

import com.dro.modules.activitycalendar.domain.ActivityCalendarDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityCalendarDailyRepository extends JpaRepository<ActivityCalendarDaily, UUID> {
    Optional<ActivityCalendarDaily> findByPlayerIdAndActivityDate(UUID playerId, LocalDate activityDate);
    List<ActivityCalendarDaily> findByPlayerIdAndActivityDateBetweenOrderByActivityDateAsc(UUID playerId, LocalDate start, LocalDate end);
    long countByPlayerIdAndActivityDateBetweenAndRewardClaimedAtIsNotNull(UUID playerId, LocalDate start, LocalDate end);
}
