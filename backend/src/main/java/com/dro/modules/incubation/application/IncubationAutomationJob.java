package com.dro.modules.incubation.application;

import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class IncubationAutomationJob {
    private static final int BATCH_SIZE = 100;
    private static final long INTERVAL_MS = 5000L;
    private final IncubationRepository repository;
    private final ClaimIncubationUseCase claimUseCase;
    private final StartIncubationUseCase startUseCase;

    public IncubationAutomationJob(IncubationRepository repository, ClaimIncubationUseCase claimUseCase, StartIncubationUseCase startUseCase) {
        this.repository = repository;
        this.claimUseCase = claimUseCase;
        this.startUseCase = startUseCase;
    }

    @Scheduled(fixedDelay = INTERVAL_MS)
    @Transactional
    public void processReadyIncubations() {
        List<UUID> ids = repository.findIdsReadyForAutomation(List.of(IncubationStatus.IN_PROGRESS, IncubationStatus.READY), LocalDateTime.now(), PageRequest.of(0, BATCH_SIZE));
        ids.forEach(id -> {
            try { process(id); }
            catch (RuntimeException error) { pause(id); }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void process(UUID id) {
        Incubation incubation = repository.findByIdForUpdate(id).orElse(null);
        if (incubation == null || !incubation.isAutoClaimEnabled() || incubation.getStatus() != IncubationStatus.IN_PROGRESS && incubation.getStatus() != IncubationStatus.READY || incubation.getFinishAt().isAfter(LocalDateTime.now())) return;
        claimUseCase.executeForPlayer(incubation.getPlayerId(), id);
        if (incubation.isAutoRepeatEnabled()) {
            startUseCase.executeForPlayer(incubation.getPlayerId(), incubation.getSlotNumber(), incubation.getDigitamaType(), incubation.getIncubatorType(), true, true);
        }
    }

    @Transactional
    void pause(UUID id) {
        repository.findByIdForUpdate(id).ifPresent(i -> { i.setAutoClaimEnabled(false); i.setAutoRepeatEnabled(false); repository.save(i); });
    }
}
