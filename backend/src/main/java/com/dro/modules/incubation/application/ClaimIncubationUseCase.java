package com.dro.modules.incubation.application;

import com.dro.modules.digimon.domain.*;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimIncubationUseCase {

    private final IncubationRepository incubationRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;

    private final Random random = new Random();

    public void execute(String token) {

        UUID playerId = extractPlayerId(token);

        Incubation incubation = findActiveIncubation(playerId);

        validateIncubationFinished(incubation);

        Digimon digimon = createDigimonFromIncubation(playerId, incubation);

        digimonRepository.save(digimon);

        setActiveIfFirstDigimon(playerId, digimon);

        finalizeIncubation(incubation);
    }

    private UUID extractPlayerId(String token) {
        return UUID.fromString(token.split(":")[1]);
    }

    private Incubation findActiveIncubation(UUID playerId) {

        return incubationRepository
                .findByPlayerIdAndStatus(playerId, IncubationStatus.IN_PROGRESS)
                .orElseThrow(() -> new RuntimeException("No active incubation"));
    }

    private void validateIncubationFinished(Incubation incubation) {

        if (incubation.getFinishAt().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Incubation not finished yet");
        }

        incubation.markReadyIfFinished();
    }

    private Digimon createDigimonFromIncubation(
            UUID playerId,
            Incubation incubation
    ) {

        Rarity rarity = RarityRoller.roll();

        int minIv = RarityRules.getMinimumIv(rarity);

        int ivHp = minIv + random.nextInt(32 - minIv);
        int ivAttack = minIv + random.nextInt(32 - minIv);
        int ivDefense = minIv + random.nextInt(32 - minIv);

        double statMultiplier = RarityRules.getStatMultiplier(rarity);

        int hp = (int) Math.floor((10 + ivHp) * statMultiplier);
        int attack = (int) Math.floor((5 + ivAttack) * statMultiplier);
        int defense = (int) Math.floor((5 + ivDefense) * statMultiplier);

        return Digimon.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .name("Baby " + incubation.getDigitamaType().name())
                .type(incubation.getDigitamaType().name())
                .stage(Stage.BABY)
                .rarity(rarity)
                .level(1)
                .experience(0)
                .ivHp(ivHp)
                .ivAttack(ivAttack)
                .ivDefense(ivDefense)
                .hp(hp)
                .attack(attack)
                .defense(defense)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void setActiveIfFirstDigimon(UUID playerId, Digimon digimon) {

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

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
