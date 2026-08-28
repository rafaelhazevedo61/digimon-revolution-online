package com.dro.modules.incubation.application;

import com.dro.modules.digitama.domain.DigitamaPool;
import com.dro.modules.digitama.domain.DigitamaPoolEntry;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.tutorial.application.TutorialService;
import com.dro.shared.security.JwtTestToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimIncubationUseCaseTest {
    @Mock
    private IncubationRepository incubationRepository;

    @Mock
    private DigimonRepository digimonRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private com.dro.modules.digitama.infra.DigitamaPoolRepository digitamaPoolRepository;

    @Mock
    private com.dro.modules.digitama.application.DigitamaPoolEligibilityService digitamaPoolEligibilityService;

    @Mock
    private TutorialService tutorialService;

    @Test
    void claimsOnlyTheRequestedIncubation() {
        UUID playerId = UUID.randomUUID();
        UUID activeDigimonId = UUID.randomUUID();
        UUID incubationId = UUID.randomUUID();
        Player player = Player.builder()
                .id(playerId)
                .activeDigimonId(activeDigimonId)
                .build();
        Incubation incubation = Incubation.builder()
                .id(incubationId)
                .playerId(playerId)
                .slotNumber(2)
                .digitamaType(com.dro.modules.inventory.domain.ItemType.DIGITAMA_FIRE)
                .incubatorType(com.dro.modules.inventory.domain.ItemType.INCUBATOR_COMMON)
                .status(IncubationStatus.READY)
                .startedAt(LocalDateTime.now().minusMinutes(5))
                .finishAt(LocalDateTime.now().minusMinutes(1))
                .build();
        DigimonInfos infos = new DigimonInfos();
        infos.setId(1L);
        infos.setName("Agumon");
        infos.setStage(com.dro.modules.digimon.domain.enums.Stage.BABY);
        infos.setElement(com.dro.modules.digimon.domain.enums.Element.FIRE);
        infos.setBaseHp(100);
        infos.setBaseAtk(50);
        infos.setBaseDef(40);
        DigitamaPoolEntry entry = DigitamaPoolEntry.builder()
                .digimonInfo(infos)
                .weight(1)
                .active(true)
                .build();
        DigitamaPool pool = DigitamaPool.builder()
                .code("DIGITAMA_FIRE")
                .entries(List.of(entry))
                .build();

        when(playerRepository.findByIdForUpdate(playerId)).thenReturn(Optional.of(player));
        when(incubationRepository.findByIdAndPlayerIdForUpdate(incubationId, playerId))
                .thenReturn(Optional.of(incubation));
        when(digitamaPoolRepository.findByCodeAndActiveTrueAndContentActiveTrue(anyString()))
                .thenReturn(Optional.of(pool));
        when(digitamaPoolEligibilityService.getEligibleEntries(pool))
                .thenReturn(List.of(entry));

        Digimon digimon = new ClaimIncubationUseCase(
                incubationRepository,
                digimonRepository,
                playerRepository,
                digitamaPoolRepository,
                digitamaPoolEligibilityService,
                tutorialService,
                null
        ).execute(JwtTestToken.create(playerId), incubationId);

        assertNotNull(digimon);
        assertEquals(playerId, digimon.getPlayerId());
        assertEquals(DigimonStatus.HATCHED, digimon.getStatus());
        assertEquals(IncubationStatus.CLAIMED, incubation.getStatus());
        verify(incubationRepository).findByIdAndPlayerIdForUpdate(eq(incubationId), eq(playerId));
        verify(incubationRepository).save(incubation);
        verify(digimonRepository).save(any(Digimon.class));
    }
}
