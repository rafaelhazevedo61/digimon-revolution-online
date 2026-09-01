package com.dro.modules.mission.application;

import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Executa a automação de missões no servidor, sem depender da tela aberta. */
@Service
public class MissionAutomationJob {
    private static final int BATCH_SIZE = 100;
    private static final long RUN_INTERVAL_MILLIS = 5000L;
    private static final Logger log = LoggerFactory.getLogger(MissionAutomationJob.class);

    private final MissionInstanceRepository missionInstanceRepository;
    private final MissionAutomationProcessor processor;

    public MissionAutomationJob(
            MissionInstanceRepository missionInstanceRepository,
            MissionAutomationProcessor processor
    ) {
        this.missionInstanceRepository = missionInstanceRepository;
        this.processor = processor;
    }

    @Scheduled(fixedDelay = RUN_INTERVAL_MILLIS)
    public void processReadyMissions() {
        Instant now = Instant.now();
        List<UUID> missionIds = missionInstanceRepository.findIdsReadyForAutomaticClaim(
                List.of(MissionStatus.RUNNING, MissionStatus.COMPLETED),
                now,
                PageRequest.of(0, BATCH_SIZE)
        );
        missionIds.forEach(id -> {
            try {
                processor.process(id);
            } catch (RuntimeException exception) {
                if (isStackLimitError(exception)) {
                    processor.pauseAutomation(id);
                    log.warn("Paused mission automation for {} because the inventory stack is full", id);
                } else {
                    log.error("Could not process automatic mission {}", id, exception);
                }
            }
        });
    }

    private boolean isStackLimitError(Throwable exception) {
        String message = exception.getMessage();
        return message != null && (message.contains("limite máximo")
                || message.contains("stack limit exceeded")
                || message.toLowerCase().contains("limite de stack"));
    }
}
