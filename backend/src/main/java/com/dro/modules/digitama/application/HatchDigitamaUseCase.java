package com.dro.modules.digitama.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonFactory;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.digitama.domain.DigitamaHistory;
import com.dro.modules.digitama.domain.DigitamaPool;
import com.dro.modules.digitama.domain.DigitamaPoolEntry;
import com.dro.modules.digitama.domain.DigitamaPoolRoller;
import com.dro.modules.digitama.domain.enums.HatchSource;
import com.dro.modules.digitama.infra.DigitamaHistoryRepository;
import com.dro.modules.digitama.infra.DigitamaPoolRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.tutorial.application.TutorialService;
import com.dro.modules.tutorial.domain.TutorialStep;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Digitama.
 */
@Service
public class HatchDigitamaUseCase {
    private static final String STARTER_DIGITAMA_POOL_CODE = "DIGITAMA_STARTER";
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final DigitamaHistoryRepository historyRepository;
    private final DigitamaPoolRepository digitamaPoolRepository;
    private final TutorialService tutorialService;

    @Transactional
    public Digimon execute(String token) {
        try {
            UUID playerId = TokenExtractor.extractPlayerId(token);
            Player player = playerRepository.findById(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
            if (player.getSelectedDigitama() == null) {
                throw new BadRequestException("No digitama selected");
            }
            String selectedDigitamaCode = player.getSelectedDigitama().getPoolCode();
            DigitamaPool pool = digitamaPoolRepository.findByCodeAndActiveTrueAndContentActiveTrue(selectedDigitamaCode).orElseThrow(() -> new NotFoundException("Digitama pool not found or inactive: " + selectedDigitamaCode));
            DigitamaPoolEntry selectedEntry = DigitamaPoolRoller.roll(pool.getEntries());
            DigimonInfos infos = selectedEntry.getDigimonInfo();
            Digimon digimon = DigimonFactory.createBaby(playerId, player.getSelectedDigitama(), infos);
            if (digimon == null) {
                throw new BadRequestException("Failed create digimon from digitama");
            }
            digimonRepository.save(digimon);
            historyRepository.save(DigitamaHistory.builder().id(UUID.randomUUID()).playerId(playerId).digitamaType(player.getSelectedDigitama()).digimonName(infos.getName()).digimonId(digimon.getId()).hatchedAt(LocalDateTime.now()).source(HatchSource.DIRECT_HATCH).build());
            if (player.getActiveDigimonId() == null) {
                player.setActiveDigimonId(digimon.getId());
            }
            player.setSelectedDigitama(null);
            playerRepository.save(player);
            tutorialService.completeStep(playerId, TutorialStep.HATCH_DIGIMON);
            return digimon;
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public HatchDigitamaUseCase(final PlayerRepository playerRepository, final DigimonRepository digimonRepository, final DigitamaHistoryRepository historyRepository, final DigitamaPoolRepository digitamaPoolRepository, final TutorialService tutorialService) {
        this.playerRepository = playerRepository;
        this.digimonRepository = digimonRepository;
        this.historyRepository = historyRepository;
        this.digitamaPoolRepository = digitamaPoolRepository;
        this.tutorialService = tutorialService;
    }
}
