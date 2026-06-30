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
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
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

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        boolean isAdmin = player.getUserType() == UserType.ADMIN;

        Incubation incubation = findActiveIncubation(playerId);

        if (isAdmin) {
            forceReadyIfInProgress(incubation);
        } else {
            validateIncubationFinished(incubation);
        }

        validateDigimonSlots(playerId);

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

    private void validateDigimonSlots(UUID playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        long activeCount = digimonRepository.countByPlayerIdAndStatus(playerId, DigimonStatus.ACTIVE);
        if (activeCount >= player.getMaxDigimonSlots()) {
            throw new BadRequestException(
                    "Slots de Digimon cheios (" + activeCount + "/" + player.getMaxDigimonSlots() +
                    "). Guarde um Digimon no Storage para liberar espaço.");
        }
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
}