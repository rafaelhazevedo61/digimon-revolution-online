package com.dro.modules.incubation.application;

import com.dro.modules.incubation.domain.Incubation;
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

    public IncubationAutomationJob(
            IncubationRepository repository,
            IncubationAutomationProcessor processor,
            CreateSystemMailMessageUseCase createSystemMailMessageUseCase
    ) {
        this.repository = repository;
        this.processor = processor;
        this.createSystemMailMessageUseCase = createSystemMailMessageUseCase;
    }

    @Scheduled(fixedDelay = INTERVAL_MS)
    public void processReadyIncubations() {
        List<UUID> ids = repository.findIdsReadyForAutomation(
                List.of(IncubationStatus.IN_PROGRESS, IncubationStatus.READY),
                LocalDateTime.now(),
                PageRequest.of(0, BATCH_SIZE)
        );
        ids.forEach(id -> {
            try {
                processor.process(id);
            } catch (RuntimeException error) {
                processor.pause(id);
                if (isStorageFullError(error)) {
                    repository.findById(id).ifPresent(incubation ->
                            createSystemMailMessageUseCase.create(
                                    MailMessageType.SYSTEM,
                                    "INCUBATION_AUTOMATION",
                                    incubation.getPlayerId(),
                                    id,
                                    "INCUBATION_AUTOMATION_RESUME",
                                    "Automação de incubação pausada",
                                    "A automação de incubação foi pausada porque o armazém de Digimons está lotado. Libere um espaço no armazém para continuar.",
                                    "incubation-automation:storage-full:" + id
                            )
                    );
                }
            }
        });
    }

    private boolean isStorageFullError(Throwable error) {
        String message = error.getMessage();
        return message != null && (message.toLowerCase().contains("storage cheio")
                || message.toLowerCase().contains("armazém cheio")
                || message.toLowerCase().contains("storage full"));
    }

    /* processing is delegated to IncubationAutomationProcessor */
    void process(UUID id) {
        processor.process(id);
    }

    void pause(UUID id) {
        processor.pause(id);
    }
}
