package com.dro.modules.incubation.application;

import com.dro.modules.digitama.application.DigitamaPoolEligibilityService;
import com.dro.modules.activitycalendar.application.ActivityCalendarService;
import com.dro.modules.activitycalendar.domain.ActivitySource;
import com.dro.modules.digitama.domain.DigitamaHatchRules;
import com.dro.modules.digitama.domain.DigitamaPool;
import com.dro.modules.digitama.domain.DigitamaPoolEntry;
import com.dro.modules.digitama.domain.DigitamaPoolRoller;
import com.dro.modules.digitama.infra.DigitamaPoolRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonFactory;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.tutorial.domain.TutorialStep;
import com.dro.modules.tutorial.application.TutorialService;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Incubação.
 */
@Service
public class ClaimIncubationUseCase {
    private final IncubationRepository incubationRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final DigitamaPoolRepository digitamaPoolRepository;
    private final DigitamaPoolEligibilityService digitamaPoolEligibilityService;
    private final TutorialService tutorialService;
    private final ActivityCalendarService activityCalendarService;

    @Transactional
    public Digimon execute(String token, UUID incubationId) {
        return executeForPlayer(TokenExtractor.extractPlayerId(token), incubationId);
    }

    @Transactional
    public Digimon executeForPlayer(UUID playerId, UUID incubationId) {
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        Incubation incubation = incubationRepository.findByIdAndPlayerIdForUpdate(incubationId, playerId)
                .orElseThrow(() -> new NotFoundException("Incubation not found"));

        if (incubation.getStatus() == IncubationStatus.CLAIMED) {
            throw new BadRequestException("Incubation already claimed");
        }

        boolean isAdmin = player.getUserType() == UserType.ADMIN;
        if (isAdmin) {
            forceReadyIfInProgress(incubation);
        } else {
            validateIncubationFinished(incubation);
        }

        Digimon digimon = createDigimonFromIncubation(playerId, incubation);
        digimonRepository.save(digimon);
        finalizeIncubation(incubation);
        tutorialService.completeStep(playerId, TutorialStep.HATCH_DIGIMON);
        if (activityCalendarService != null) {
            activityCalendarService.recordActivity(playerId, ActivitySource.DIGITAMA_HATCHED, incubationId.toString());
        }
        return digimon;
    }

    private void validateIncubationFinished(Incubation incubation) {
        if (incubation.getStatus() == IncubationStatus.READY) {
            return;
        }
        if (incubation.getStatus() != IncubationStatus.IN_PROGRESS) {
            throw new BadRequestException("Incubation is not available");
        }
        if (incubation.getFinishAt().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Incubation not finished yet");
        }
        incubation.markReadyIfFinished();
    }

    private Digimon createDigimonFromIncubation(UUID playerId, Incubation incubation) {
        var digitamaType = DigitamaHatchRules.toDigitamaType(incubation.getDigitamaType());
        String poolCode = digitamaType.getPoolCode();
        DigitamaPool pool = digitamaPoolRepository.findByCodeAndActiveTrueAndContentActiveTrue(poolCode)
                .orElseThrow(() -> new NotFoundException("Digitama pool not found: " + poolCode));
        DigitamaPoolEntry entry = DigitamaPoolRoller.roll(
                digitamaPoolEligibilityService.getEligibleEntries(pool)
        );
        var infos = entry.getDigimonInfo();
        return DigimonFactory.createBaby(playerId, digitamaType, infos, DigimonStatus.HATCHED);
    }


    private void forceReadyIfInProgress(Incubation incubation) {
        if (incubation.getStatus() == IncubationStatus.IN_PROGRESS) {
            incubation.setStatus(IncubationStatus.READY);
        }
    }

    private void finalizeIncubation(Incubation incubation) {
        incubation.claim();
        incubationRepository.save(incubation);
    }

    public ClaimIncubationUseCase(
            final IncubationRepository incubationRepository,
            final DigimonRepository digimonRepository,
            final PlayerRepository playerRepository,
            final DigitamaPoolRepository digitamaPoolRepository,
            final DigitamaPoolEligibilityService digitamaPoolEligibilityService,
            final TutorialService tutorialService,
            final ActivityCalendarService activityCalendarService
    ) {
        this.incubationRepository = incubationRepository;
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
        this.digitamaPoolRepository = digitamaPoolRepository;
        this.digitamaPoolEligibilityService = digitamaPoolEligibilityService;
        this.tutorialService = tutorialService;
        this.activityCalendarService = activityCalendarService;
    }

}
