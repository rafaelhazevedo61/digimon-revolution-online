package com.dro.modules.incubation.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
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

    public void execute(String token) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        Incubation incubation = incubationRepository
                .findByPlayerIdAndStatus(playerId, IncubationStatus.IN_PROGRESS)
                .orElseThrow(() -> new RuntimeException("No active incubation"));

        // 1️⃣ Verificar tempo
        incubation.markReadyIfFinished();

        // 2️⃣ Criar Digimon
        Random random = new Random();

        int ivHp = random.nextInt(32);
        int ivAttack = random.nextInt(32);
        int ivDefense = random.nextInt(32);

        Digimon digimon = Digimon.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .name("Baby " + incubation.getDigitamaType().name())
                .type(incubation.getDigitamaType().name())
                .stage(Stage.BABY)
                .level(1)
                .experience(0)
                .ivHp(ivHp)
                .ivAttack(ivAttack)
                .ivDefense(ivDefense)
                .hp(10 + ivHp)
                .attack(5 + ivAttack)
                .defense(5 + ivDefense)
                .createdAt(LocalDateTime.now())
                .build();

        digimonRepository.save(digimon);

        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            player.setActiveDigimonId(digimon.getId());
            playerRepository.save(player);
        }

        // 3️⃣ Marcar como CLAIMED
        incubation.claim();
        incubationRepository.save(incubation);
    }
}