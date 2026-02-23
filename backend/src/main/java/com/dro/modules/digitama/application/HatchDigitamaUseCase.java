package com.dro.modules.digitama.application;

import com.dro.modules.digimon.domain.*;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HatchDigitamaUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;

    private final Random random = new Random();

    public void execute (String token) {

        UUID playerId = extractPlayerId(token);

        Player player = findPlayer(playerId);

        validateDigitamaSelection(player);

        Digimon digimon = createDigimon(playerId, player);

        digimonRepository.save(digimon);

        setActiveIfFirstDigimon(player, digimon);

        clearSelectedDigitama(player);
    }

    private UUID extractPlayerId (String token) {
        return UUID.fromString(token.split(":")[1]);
    }

    private Player findPlayer (UUID playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
    }

    private void validateDigitamaSelection (Player player) {
        if (player.getSelectedDigitama() == null) {
            throw new RuntimeException("Digitama already hatched or not selected");
        }
    }

    private Digimon createDigimon (UUID playerId, Player player) {

        // 1️⃣ Rolar raridade
        Rarity rarity = RarityRoller.roll();

        // 2️⃣ Rolar personalidade
        Personality personality = PersonalityRoller.roll();

        // 3️⃣ Definir IV mínimo baseado na raridade
        int minIv = RarityRules.getMinimumIv(rarity);

        int ivHp = minIv + random.nextInt(32 - minIv);
        int ivAttack = minIv + random.nextInt(32 - minIv);
        int ivDefense = minIv + random.nextInt(32 - minIv);

        // 4️⃣ Multiplicadores
        double rarityMultiplier = RarityRules.getStatMultiplier(rarity);
        double stageMultiplier = EvolutionRules.stageStatMultiplier(Stage.BABY);

        double hpMultiplier =
                rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getHpMultiplier(personality);

        double attackMultiplier =
                rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getAttackMultiplier(personality);

        double defenseMultiplier =
                rarityMultiplier
                        * stageMultiplier
                        * PersonalityRules.getDefenseMultiplier(personality);

        // 5️⃣ Cálculo final de stats
        int hp = (int) Math.floor((10 + ivHp) * hpMultiplier);
        int attack = (int) Math.floor((5 + ivAttack) * attackMultiplier);
        int defense = (int) Math.floor((5 + ivDefense) * defenseMultiplier);


        return Digimon.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .name("Baby " + player.getSelectedDigitama().name())
                .type(player.getSelectedDigitama().name())
                .stage(Stage.BABY)
                .rarity(rarity)
                .personality(personality)
                .level(1)
                .experience(0)
                .ivHp(ivHp)
                .ivAttack(ivAttack)
                .ivDefense(ivDefense)
                .hp(hp)
                .attack(attack)
                .defense(defense)
                .createdAt(LocalDateTime.now())
                .energy(20)
                .maxEnergy(20)
                .lastEnergyUpdate(Instant.now())
                .build();
    }

    private void setActiveIfFirstDigimon (Player player, Digimon digimon) {

        if (player.getActiveDigimonId() == null) {
            player.setActiveDigimonId(digimon.getId());
            playerRepository.save(player);
        }
    }

    private void clearSelectedDigitama (Player player) {
        player.setSelectedDigitama(null);
        playerRepository.save(player);
    }
}
