package com.dro.modules.digitama.application;

import com.dro.modules.digitama.domain.DigitamaPool;
import com.dro.modules.digitama.domain.enums.DigitamaType;
import com.dro.modules.digitama.infra.DigitamaPoolRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.tutorial.application.TutorialService;
import com.dro.modules.tutorial.domain.TutorialStep;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Digitama.
 */
@Service
public class SelectDigitamaUseCase {
    private final PlayerRepository repository;
    private final DigitamaPoolRepository digitamaPoolRepository;
    private final DigitamaPoolEligibilityService digitamaPoolEligibilityService;
    private final TutorialService tutorialService;

    @Transactional
    public void execute(String token, DigitamaType type) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = repository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.hasSelectedStarter()) {
            throw new ConflictException("Starter already selected");
        }
        if (player.getSelectedDigitama() != null) {
            throw new ConflictException("Digitama already selected");
        }
        DigitamaPool pool = digitamaPoolRepository.findByCodeAndActiveTrueAndContentActiveTrue(type.getPoolCode())
                .orElseThrow(() -> new NotFoundException("Digitama pool not found or inactive: " + type.getPoolCode()));
        if (digitamaPoolEligibilityService.getEligibleEntries(pool).isEmpty()) {
            throw new BadRequestException("Selected digitama is not available");
        }
        player.setSelectedDigitama(type);
        player.markStarterAsSelected();
        repository.save(player);
        tutorialService.completeStep(playerId, TutorialStep.SELECT_DIGITAMA);
    }

    public SelectDigitamaUseCase(
            final PlayerRepository repository,
            final DigitamaPoolRepository digitamaPoolRepository,
            final DigitamaPoolEligibilityService digitamaPoolEligibilityService,
            final TutorialService tutorialService
    ) {
        this.repository = repository;
        this.digitamaPoolRepository = digitamaPoolRepository;
        this.digitamaPoolEligibilityService = digitamaPoolEligibilityService;
        this.tutorialService = tutorialService;
    }
}
