package com.dro.modules.event.infra;

import com.dro.modules.event.domain.EventRewardItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Persistência dos itens individuais de uma premiação. */
public interface EventRewardItemRepository extends JpaRepository<EventRewardItem, UUID> {
    List<EventRewardItem> findByEventRewardIdOrderByPositionAsc(UUID eventRewardId);
}
