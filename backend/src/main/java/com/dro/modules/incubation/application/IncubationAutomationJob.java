package com.dro.modules.incubation.application;

import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class IncubationAutomationJob {
    private static final int BATCH_SIZE = 100;
    private static final long INTERVAL_MS = 5000L;
    private final IncubationRepository repository;
    private final IncubationAutomationProcessor processor;

    public IncubationAutomationJob(IncubationRepository repository, IncubationAutomationProcessor processor) {
        this.repository = repository;
        this.processor = processor;
    }

    @Scheduled(fixedDelay = INTERVAL_MS)
    public void processReadyIncubations() {
        List<UUID> ids = repository.findIdsReadyForAutomation(List.of(IncubationStatus.IN_PROGRESS, IncubationStatus.READY), LocalDateTime.now(), PageRequest.of(0, BATCH_SIZE));
        ids.forEach(id -> {
            try { processor.process(id); }
            catch (RuntimeException error) { processor.pause(id); }
        });
    }

    /* processing is delegated to IncubationAutomationProcessor */
    void process(UUID id) {
        processor.process(id);
    }

    void pause(UUID id) {
        processor.pause(id);
    }
}
