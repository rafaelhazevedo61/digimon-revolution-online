package com.dro.modules.mission.application;

import com.dro.modules.mail.application.CreateSystemMailMessageUseCase;
import com.dro.shared.automation.AutomationFailureCode;
import com.dro.shared.automation.AutomationFailureException;
import com.dro.shared.observability.AutomationMetrics;
import io.micrometer.core.instrument.Timer;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Executa a automação de missões no servidor, sem depender da tela aberta. */
@Service
public class MissionAutomationJob {
    private static final int BATCH_SIZE = 100;
    private static final long RUN_INTERVAL_MILLIS = 5000L;
    private static final Logger log = LoggerFactory.getLogger(MissionAutomationJob.class);
    private static final Pattern STACK_ITEM_PATTERN = Pattern.compile("item ['\\\"](.+?)['\\\"]", Pattern.CASE_INSENSITIVE);

    private final MissionInstanceRepository missionInstanceRepository;
    private final MissionAutomationProcessor processor;
    private final CreateSystemMailMessageUseCase createSystemMailMessageUseCase;
    private final AutomationMetrics automationMetrics;

    public MissionAutomationJob(
            MissionInstanceRepository missionInstanceRepository,
            MissionAutomationProcessor processor,
            CreateSystemMailMessageUseCase createSystemMessageUseCase,
            AutomationMetrics automationMetrics
    ) {
        this.missionInstanceRepository = missionInstanceRepository;
        this.processor = processor;
        this.createSystemMailMessageUseCase = createSystemMessageUseCase;
        this.automationMetrics = automationMetrics;
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
            Timer.Sample sample = automationMetrics.startRun("mission");
            try {
                processor.process(id);
            } catch (RuntimeException exception) {
                AutomationFailureCode failureCode = findFailureCode(exception);
                automationMetrics.recordFailure("mission", failureCode != null ? failureCode.name() : AutomationFailureCode.TRANSIENT_DATABASE_ERROR.name());
                if (failureCode == AutomationFailureCode.INVENTORY_STACK_FULL || isStackLimitError(exception)) {
                    UUID playerId = findPlayerId(id);
                    processor.pauseAutomation(id, "INVENTORY_STACK_FULL", AutomationFailureCode.INVENTORY_STACK_FULL.name());
                    automationMetrics.recordPause("mission", "INVENTORY_STACK_FULL");
                    if (playerId != null) {
                        createSystemMailMessageUseCase.create(
                                MailMessageType.SYSTEM,
                                "MISSION_AUTOMATION",
                                playerId,
                                id,
                                "MISSION_AUTOMATION_RESUME",
                                "Automação de missão pausada",
                                buildStackLimitMailBody(exception),
                                "mission-automation:inventory-full:" + id
                        );
                        automationMetrics.recordSystemMail("mission", "INVENTORY_STACK_FULL");
                    } else {
                        log.error("Could not notify player because mission {} was not found", id);
                    }
                    log.warn("Paused mission automation for {} because the inventory stack is full", id);
                } else {
                    processor.pauseAutomation(id, "AUTOMATION_ERROR", AutomationFailureCode.TRANSIENT_DATABASE_ERROR.name());
                    automationMetrics.recordPause("mission", "TRANSIENT_DATABASE_ERROR");
                    log.error("Could not process automatic mission {}; automation paused", id, exception);
                }
            } finally {
                automationMetrics.stopRun("mission", sample);
            }
        });
    }

    private UUID findPlayerId(UUID missionInstanceId) {
        return missionInstanceRepository.findById(missionInstanceId)
                .map(instance -> instance.getPlayerId())
                .orElse(null);
    }

    private String buildStackLimitMailBody(Throwable exception) {
        String itemName = extractStackLimitItemName(exception);
        if (itemName == null) {
            return "A automação desta missão foi pausada porque um item atingiu o limite máximo do inventário. Libere espaço ou reduza a quantidade do item para continuar.";
        }
        return "A automação desta missão foi pausada porque o item \"" + itemName
                + "\" atingiu o limite máximo do inventário. Libere espaço ou reduza a quantidade desse item para continuar.";
    }

    private String extractStackLimitItemName(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                Matcher matcher = STACK_ITEM_PATTERN.matcher(message);
                if (matcher.find()) return matcher.group(1);
            }
            current = current.getCause();
        }
        return null;
    }

    private AutomationFailureCode findFailureCode(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof AutomationFailureException typed) return typed.getFailureCode();
            current = current.getCause();
        }
        return null;
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
