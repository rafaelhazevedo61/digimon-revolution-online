package com.dro.modules.incubation.application;

import com.dro.modules.incubation.domain.Incubation;
import com.dro.shared.automation.AutomationFailureCode;
import com.dro.shared.automation.AutomationFailureException;
import com.dro.shared.observability.AutomationMetrics;
import io.micrometer.core.instrument.Timer;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.mail.application.CreateSystemMailMessageUseCase;
import com.dro.modules.mail.domain.MailMessageType;
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
    private final CreateSystemMailMessageUseCase createSystemMailMessageUseCase;
    private final AutomationMetrics automationMetrics;

    public IncubationAutomationJob(
            IncubationRepository repository,
            IncubationAutomationProcessor processor,
            CreateSystemMailMessageUseCase createSystemMailMessageUseCase,
            AutomationMetrics automationMetrics
    ) {
        this.repository = repository;
        this.processor = processor;
        this.createSystemMailMessageUseCase = createSystemMailMessageUseCase;
        this.automationMetrics = automationMetrics;
    }

    @Scheduled(fixedDelay = INTERVAL_MS)
    public void processReadyIncubations() {
        List<UUID> ids = repository.findIdsReadyForAutomation(
                List.of(IncubationStatus.IN_PROGRESS, IncubationStatus.READY),
                LocalDateTime.now(),
                PageRequest.of(0, BATCH_SIZE)
        );
        ids.forEach(id -> {
            Timer.Sample sample = automationMetrics.startRun("incubation");
            try {
                processor.process(id);
            } catch (RuntimeException error) {
                AutomationFailureCode failureCode = findFailureCode(error);
                automationMetrics.recordFailure("incubation", failureCode != null ? failureCode.name() : AutomationFailureCode.TRANSIENT_DATABASE_ERROR.name());
                processor.pause(id, failureCode != null ? failureCode.name() : "AUTOMATION_ERROR",
                        failureCode != null ? failureCode.name() : AutomationFailureCode.TRANSIENT_DATABASE_ERROR.name());
                if (failureCode == AutomationFailureCode.DIGIMON_STORAGE_FULL || isStorageFullError(error)) {
                    automationMetrics.recordPause("incubation", "DIGIMON_STORAGE_FULL");
                    repository.findById(id).ifPresent(incubation -> {
                        createSystemMailMessageUseCase.create(
                                MailMessageType.SYSTEM,
                                "INCUBATION_AUTOMATION",
                                incubation.getPlayerId(),
                                id,
                                "INCUBATION_AUTOMATION_RESUME",
                                "Automação de incubação pausada",
                                "A automação de incubação foi pausada porque o armazém de Digimons está lotado. Libere um espaço no armazém para continuar.",
                                "incubation-automation:storage-full:" + id
                        );
                        automationMetrics.recordSystemMail("incubation", "DIGIMON_STORAGE_FULL");
                    });
                }
            } finally {
                automationMetrics.stopRun("incubation", sample);
            }
        });
    }

    private AutomationFailureCode findFailureCode(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof AutomationFailureException typed) return typed.getFailureCode();
            current = current.getCause();
        }
        return null;
    }

    private boolean isStorageFullError(Throwable error) {
        Throwable current = error;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase();
                if (normalized.contains("storage cheio")
                        || normalized.contains("armazém cheio")
                        || normalized.contains("storage full")) return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /* processing is delegated to IncubationAutomationProcessor */
    void process(UUID id) {
        processor.process(id);
    }

    void pause(UUID id) {
        processor.pause(id);
    }
}
