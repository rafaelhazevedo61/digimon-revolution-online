package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonGrade;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.domain.enums.Personality;
import com.dro.modules.digimon.domain.enums.Rarity;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.mission.api.dto.response.AreaResponse;
import com.dro.modules.mission.domain.Area;
import com.dro.shared.security.JwtSettings;
import com.dro.shared.security.JwtTokenCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AreaUseCaseTest {

    @Mock
    private DigimonRepository digimonRepository;

    @Test
    void unlocksAreasUsingOnlyTheActiveDigimonStage() {
        UUID playerId = UUID.randomUUID();
        Digimon activeBaby = digimon(playerId, Stage.BABY, DigimonStatus.ACTIVE);
        Digimon storedMega = digimon(playerId, Stage.MEGA, DigimonStatus.STORED);
        when(digimonRepository.findByPlayerIdAndStatus(playerId, DigimonStatus.ACTIVE))
                .thenReturn(List.of(activeBaby));

        List<AreaResponse> areas = new AreaUseCase(digimonRepository).execute(createToken(playerId));

        assertTrue(area(areas, Area.NATIVE_FOREST).unlocked());
        assertFalse(area(areas, Area.FREEZELAND).unlocked());
        assertFalse(area(areas, Area.SERVER_DESERT).unlocked());
        assertFalse(area(areas, Area.INFINITY_MOUNTAIN).unlocked());
    }

    private AreaResponse area(List<AreaResponse> areas, Area target) {
        return areas.stream().filter(response -> response.area() == target).findFirst().orElseThrow();
    }

    private Digimon digimon(UUID playerId, Stage stage, DigimonStatus status) {
        return Digimon.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .name("Testmon")
                .type("FIRE")
                .stage(stage)
                .level(1)
                .experience(0)
                .hp(100)
                .attack(50)
                .defense(50)
                .grade(DigimonGrade.C)
                .rarity(Rarity.COMMON)
                .personality(Personality.FIGHTER)
                .energy(20)
                .maxEnergy(20)
                .lastEnergyUpdate(Instant.now())
                .bits(0)
                .rebirthCount(0)
                .arenaRating(1000)
                .arenaWins(0)
                .arenaLosses(0)
                .bot(false)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private static String createToken(UUID subject) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject.toString());
        claims.put("iss", JwtSettings.getIssuer());
        claims.put("exp", Instant.now().getEpochSecond() + 3600);
        return JwtTokenCodec.create(claims, JwtSettings.getSecret());
    }
}
