package com.dro.modules.incubation.application;

import com.dro.modules.digitama.domain.DigitamaHatchRules;
import com.dro.modules.digitama.domain.DigitamaPool;
import com.dro.modules.digitama.domain.DigitamaPoolEntry;
import com.dro.modules.digitama.domain.DigitamaPoolRoller;
import com.dro.modules.digitama.domain.enums.DigitamaType;
import com.dro.modules.digitama.infra.DigitamaPoolRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonFactory;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimIncubationUseCase {

    private final IncubationRepository incubationRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final DigitamaPoolRepository digitamaPoolRepository;

    public Digimon execute(String token) {

        UUID playerId = extractPlayerId(token);

        Incubation incubation = findActiveIncubation(playerId);

        validateIncubationFinished(incubation);

        Digimon digimon = createDigimonFromIncubation(playerId, incubation);

        digimonRepository.save(digimon);

        setActiveIfFirstDigimon(playerId, digimon);

        finalizeIncubation(incubation);

        return digimon;
    }

    private UUID extractPlayerId(String token) {
        return TokenExtractor.extractPlayerId(token);
    }

    private Incubation findActiveIncubation(UUID playerId) {

        return incubationRepository
                .findByPlayerIdAndStatus(playerId, IncubationStatus.READY)
                .or(() -> incubationRepository.findByPlayerIdAndStatus(playerId, IncubationStatus.IN_PROGRESS))
                .orElseThrow(() -> new NotFoundException("No active incubation"));
    }

    private void validateIncubationFinished(Incubation incubation) {

        if (incubation.getStatus() == IncubationStatus.READY) {
            return;
        }

        if (incubation.getFinishAt().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Incubation not finished yet");
        }

        incubation.markReadyIfFinished();
    }

    private Digimon createDigimonFromIncubation(
            UUID playerId,
            Incubation incubation
    ) {
        DigitamaType digitamaType = DigitamaHatchRules.toDigitamaType(incubation.getDigitamaType());
        String poolCode = digitamaType.getPoolCode();

        DigitamaPool pool = digitamaPoolRepository
                .findByCodeAndActiveTrueAndContentActiveTrue(poolCode)
                .orElseThrow(() -> new NotFoundException("Digitama pool not found: " + poolCode));

        DigitamaPoolEntry entry = DigitamaPoolRoller.roll(pool.getEntries());
        DigimonInfos infos = entry.getDigimonInfo();

        return DigimonFactory.createBaby(playerId, digitamaType, infos);
    }

    private void setActiveIfFirstDigimon(UUID playerId, Digimon digimon) {

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            player.setActiveDigimonId(digimon.getId());
            playerRepository.save(player);
        }
    }

    private void finalizeIncubation(Incubation incubation) {

        incubation.claim();
        incubationRepository.save(incubation);
    }
}