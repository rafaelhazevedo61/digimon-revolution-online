package com.dro.modules.mission.application;

import com.dro.modules.mail.application.CreateSystemMailMessageUseCase;
import com.dro.modules.mail.domain.MailMessageType;
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
    private final CreateSystemMailMessageUseCase createSystemMailMessageUseCase;

    public MissionAutomationJob(
            MissionInstanceRepository missionInstanceRepository,
            MissionAutomationProcessor processor,
            CreateSystemMailMessageUseCase createSystemMailMessageUseCase
    ) {
        this.missionInstanceRepository = missionInstanceRepository;
        this.processor = processor;
        this.createSystemMailMessageUseCase = createSystemMailMessageUseCase;
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
                    UUID playerId = findPlayerId(id);
                    processor.pauseAutomation(id);
                    if (playerId != null) {
                        createSystemMailMessageUseCase.create(
                                MailMessageType.SYSTEM,
                                "MISSION_AUTOMATION",
                                playerId,
                                id,
                                "MISSION_AUTOMATION_RESUME",
                                "Automação de missão pausada",
                                "A automação desta missão foi pausada porque um item atingiu o limite máximo do inventário. Libere espaço ou reduza a quantidade do item para continuar.",
                                "mission-automation:inventory-full:" + id
                        );
                    } else {
                        log.error("Could not notify player because mission {} was not found", id);
                    }
                    log.warn("Paused mission automation for {} because the inventory stack is full", id);
                } else {
                    log.error("Could not process automatic mission {}", id, exception);
                }
            }
        });
    }

    private UUID findPlayerId(UUID missionInstanceId) {
        return missionInstanceRepository.findById(missionInstanceId)
                .map(instance -> instance.getPlayerId())
                .orElse(null);
    }

    private boolean isStackLimitError(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase();
                if (normalized.contains("stack limit exceeded")
                        || normalized.contains("maximum stack")
                        || normalized.contains("limite máximo")
                        || normalized.contains("limite de stack")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
