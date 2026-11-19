package com.dro.modules.activitycalendar.infra;

import com.dro.modules.activitycalendar.domain.ActivityCalendarMonthly;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ActivityCalendarMonthlyRepository extends JpaRepository<ActivityCalendarMonthly, UUID> {
    Optional<ActivityCalendarMonthly> findByPlayerIdAndYearMonth(UUID playerId, String yearMonth);
}
