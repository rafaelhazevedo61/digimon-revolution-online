package com.dro.modules.digitama.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonFactory;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.digitama.domain.DigitamaHatchRules;
import com.dro.modules.digitama.domain.DigitamaHistory;
import com.dro.modules.digitama.domain.enums.HatchSource;
import com.dro.modules.digitama.infra.DigitamaHistoryRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HatchDigitamaUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final DigimonInfosRepository digimonInfosRepository;
    private final DigitamaHistoryRepository historyRepository;

    public Digimon execute (String token) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        String babyName = DigitamaHatchRules.rollBabyName(player.getSelectedDigitama());

        DigimonInfos infos = digimonInfosRepository.findByName(babyName)
                .orElseThrow(() -> new NotFoundException("Species not found: " + babyName));

        Digimon digimon = DigimonFactory.createBaby(playerId, player.getSelectedDigitama(), infos);

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
    }

}