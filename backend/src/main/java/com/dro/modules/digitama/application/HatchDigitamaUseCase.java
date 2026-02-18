package com.dro.modules.digitama.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HatchDigitamaUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;

    public void execute(String token) {

        UUID playerId = UUID.fromString(token.split(":")[1]);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (player.getSelectedDigitama() == null) {
            throw new RuntimeException("No digitama selected");
        }

        Random random = new Random();

        int ivHp = random.nextInt(32);
        int ivAttack = random.nextInt(32);
        int ivDefense = random.nextInt(32);

        Digimon digimon = Digimon.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .name("Baby " + player.getSelectedDigitama().name())
                .type(player.getSelectedDigitama().name())
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

        if (player.getActiveDigimonId() == null) {
            player.setActiveDigimonId(digimon.getId());
            playerRepository.save(player);
        }
    }
}
