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
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HatchDigitamaUseCase {

    private static final String STARTER_DIGITAMA_POOL_CODE = "DIGITAMA_STARTER";

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final DigitamaHistoryRepository historyRepository;
    private final DigitamaPoolRepository digitamaPoolRepository;

    public Digimon execute(String token) {

        try {

            UUID playerId = TokenExtractor.extractPlayerId(token);

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new NotFoundException("Player not found"));

            if (player.getSelectedDigitama() == null) {
                throw new BadRequestException("No digitama selected");
            }

            DigitamaPool pool = digitamaPoolRepository
                    .findByCodeAndActiveTrueAndContentActiveTrue(STARTER_DIGITAMA_POOL_CODE)
                    .orElseThrow(() -> new NotFoundException("Digitama pool not found or inactive: " + STARTER_DIGITAMA_POOL_CODE));

            DigitamaPoolEntry selectedEntry = DigitamaPoolRoller.roll(pool.getEntries());

            DigimonInfos infos = selectedEntry.getDigimonInfo();

            Digimon digimon = DigimonFactory.createBaby(
                    playerId,
                    player.getSelectedDigitama(),
                    infos
            );

            if (digimon == null) {
                throw new BadRequestException("Failed create digimon from digitama");
            }

            digimonRepository.save(digimon);

            historyRepository.save(DigitamaHistory.builder()
                    .id(UUID.randomUUID())
                    .playerId(playerId)
                    .digitamaType(player.getSelectedDigitama())
                    .digimonName(infos.getName())
                    .digimonId(digimon.getId())
                    .hatchedAt(LocalDateTime.now())
                    .source(HatchSource.DIRECT_HATCH)
                    .build());

            if (player.getActiveDigimonId() == null) {
                player.setActiveDigimonId(digimon.getId());
            }

            player.setSelectedDigitama(null);
            playerRepository.save(player);

            return digimon;

        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}