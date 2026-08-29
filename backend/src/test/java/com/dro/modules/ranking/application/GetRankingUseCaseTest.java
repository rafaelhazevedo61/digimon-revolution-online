package com.dro.modules.ranking.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.*;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.ranking.api.dto.response.RankingEntryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRankingUseCaseTest {

    @Mock
    private DigimonRepository digimonRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private DigimonInfosRepository digimonInfosRepository;

    @BeforeEach
    void setUp() {
        when(digimonInfosRepository.findAllById(any())).thenReturn(List.of());
    }

    @InjectMocks
    private GetRankingUseCase getRankingUseCase;

    @Test
    void byLevel_returnsDigimonsOrderedByPosition() {
        UUID playerId = UUID.randomUUID();
        Digimon d1 = buildDigimon("Agumon", 50, DigimonGrade.S, 3, playerId);
        Digimon d2 = buildDigimon("Gabumon", 30, DigimonGrade.A, 1, playerId);

        Player player = Player.builder().id(playerId).username("rafael").build();

        when(digimonRepository.findByStatusInOrderByLevelDescExperienceDesc(
                eq(List.of(DigimonStatus.ACTIVE, DigimonStatus.STORED)), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(d1, d2)));

        when(playerRepository.findAllById(any())).thenReturn(List.of(player));

        List<RankingEntryResponse> result = getRankingUseCase.byLevel(0, 10);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).position());
        assertEquals("Agumon", result.get(0).digimonName());
        assertEquals(50, result.get(0).level());
        assertEquals("rafael", result.get(0).playerName());
        assertEquals(2, result.get(1).position());
        assertEquals("Gabumon", result.get(1).digimonName());
    }

    @Test
    void byLevel_includesStoredDigimons() {
        UUID playerId = UUID.randomUUID();
        Digimon active = buildDigimon("Agumon", 50, DigimonGrade.S, 3, playerId);
        Digimon stored = buildDigimon("Gabumon", 45, DigimonGrade.A, 1, playerId);
        stored.setStatus(DigimonStatus.STORED);
        Player player = Player.builder().id(playerId).username("rafael").build();

        when(digimonRepository.findByStatusInOrderByLevelDescExperienceDesc(
                eq(List.of(DigimonStatus.ACTIVE, DigimonStatus.STORED)), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(active, stored)));
        when(playerRepository.findAllById(any())).thenReturn(List.of(player));

        List<RankingEntryResponse> result = getRankingUseCase.byLevel(0, 10);

        assertEquals(2, result.size());
        assertEquals("Gabumon", result.get(1).digimonName());
    }

    @Test
    void byGrade_returnsDigimonsWithCorrectPositions() {
        UUID playerId = UUID.randomUUID();
        Digimon d1 = buildDigimon("WarGreymon", 99, DigimonGrade.SSS, 10, playerId);

        Player player = Player.builder().id(playerId).username("rafael").build();

        when(digimonRepository.findByStatusInOrderByGradeQualityAscLevelDesc(
                eq(List.of(DigimonStatus.ACTIVE, DigimonStatus.STORED)), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(d1)));

        when(playerRepository.findAllById(any())).thenReturn(List.of(player));

        List<RankingEntryResponse> result = getRankingUseCase.byGrade(0, 10);

        assertEquals(1, result.size());
        assertEquals(DigimonGrade.SSS, result.get(0).grade());
        assertEquals(99, result.get(0).level());
    }

    @Test
    void byRebirth_returnsDigimonsWithRebirthCount() {
        UUID playerId = UUID.randomUUID();
        Digimon d1 = buildDigimon("Omegamon", 80, DigimonGrade.SS, 10, playerId);
        Digimon d2 = buildDigimon("MetalGarurumon", 70, DigimonGrade.A, 5, playerId);

        Player player = Player.builder().id(playerId).username("rafael").build();

        when(digimonRepository.findByStatusInAndRebirthCountGreaterThanOrderByRebirthCountDescLevelDesc(
                eq(List.of(DigimonStatus.ACTIVE, DigimonStatus.STORED)), eq(0), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(d1, d2)));

        when(playerRepository.findAllById(any())).thenReturn(List.of(player));

        List<RankingEntryResponse> result = getRankingUseCase.byRebirth(0, 10);

        assertEquals(2, result.size());
        assertEquals(10, result.get(0).rebirthCount());
        assertEquals(5, result.get(1).rebirthCount());
    }

    @Test
    void byLevel_page1_positionsStartAt11() {
        UUID playerId = UUID.randomUUID();
        Digimon d1 = buildDigimon("Patamon", 20, DigimonGrade.B, 0, playerId);

        Player player = Player.builder().id(playerId).username("rafael").build();

        when(digimonRepository.findByStatusInOrderByLevelDescExperienceDesc(
                eq(List.of(DigimonStatus.ACTIVE, DigimonStatus.STORED)), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(d1)));

        when(playerRepository.findAllById(any())).thenReturn(List.of(player));

        List<RankingEntryResponse> result = getRankingUseCase.byLevel(1, 10);

        assertEquals(11, result.get(0).position());
    }

    @Test
    void byLevel_emptyResult_returnsEmptyList() {
        when(digimonRepository.findByStatusInOrderByLevelDescExperienceDesc(
                eq(List.of(DigimonStatus.ACTIVE, DigimonStatus.STORED)), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<RankingEntryResponse> result = getRankingUseCase.byLevel(0, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    void byLevel_unknownPlayer_returnsUnknownAsPlayerName() {
        UUID playerId = UUID.randomUUID();
        Digimon d1 = buildDigimon("Agumon", 50, DigimonGrade.A, 0, playerId);

        when(digimonRepository.findByStatusInOrderByLevelDescExperienceDesc(
                eq(List.of(DigimonStatus.ACTIVE, DigimonStatus.STORED)), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(d1)));

        when(playerRepository.findAllById(any())).thenReturn(List.of());

        List<RankingEntryResponse> result = getRankingUseCase.byLevel(0, 10);

        assertEquals("Unknown", result.get(0).playerName());
    }

    private Digimon buildDigimon(String name, int level, DigimonGrade grade, int rebirthCount, UUID playerId) {
        return Digimon.builder()
                .id(UUID.randomUUID())
                .playerId(playerId)
                .name(name)
                .type("FIRE")
                .stage(Stage.MEGA)
                .level(level)
                .experience(0)
                .hp(100)
                .attack(50)
                .defense(50)
                .ivHp(90)
                .ivAttack(85)
                .ivDefense(80)
                .grade(grade)
                .rarity(Rarity.COMMON)
                .personality(Personality.FIGHTER)
                .trait(Trait.BERSERKER)
                .energy(20)
                .maxEnergy(20)
                .lastEnergyUpdate(Instant.now())
                .bits(0)
                .rebirthCount(rebirthCount)
                .status(DigimonStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
