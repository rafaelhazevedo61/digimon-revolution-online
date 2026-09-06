package com.dro.modules.collection.application;

import com.dro.modules.collection.domain.CollectionEntry;
import com.dro.modules.collection.infra.CollectionEntryRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.domain.enums.Rarity;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionRegistrationServiceTest {
    @Mock
    private CollectionEntryRepository collectionRepository;
    @Mock
    private DigimonRepository digimonRepository;
    @Mock
    private AddItemUseCase addItemUseCase;

    @Test
    void registersSpeciesAndRarityOnlyOnce() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Digimon digimon = org.mockito.Mockito.mock(Digimon.class);
        when(digimon.getPlayerId()).thenReturn(playerId);
        when(digimon.getId()).thenReturn(digimonId);
        when(digimon.getDigimonInfoId()).thenReturn(42L);
        when(digimon.getRarity()).thenReturn(Rarity.RARE);
        when(collectionRepository.existsByPlayerIdAndDigimonInfoIdAndRarity(playerId, 42L, Rarity.RARE))
                .thenReturn(false);
        when(collectionRepository.countByPlayer(playerId)).thenReturn(3L);

        CollectionRegistrationService service = new CollectionRegistrationService(
                collectionRepository,
                digimonRepository,
                addItemUseCase
        );

        assertTrue(service.registerIfMissing(digimon, "HATCH"));

        ArgumentCaptor<CollectionEntry> captor = ArgumentCaptor.forClass(CollectionEntry.class);
        verify(collectionRepository).save(captor.capture());
        assertEquals(playerId, captor.getValue().getPlayerId());
        assertEquals(42L, captor.getValue().getDigimonInfoId());
        assertEquals(Rarity.RARE, captor.getValue().getRarity());
        assertEquals("HATCH", captor.getValue().getSourceEvent());
    }

    @Test
    void ignoresAlreadyRegisteredCombination() {
        UUID playerId = UUID.randomUUID();
        Digimon digimon = org.mockito.Mockito.mock(Digimon.class);
        when(digimon.getPlayerId()).thenReturn(playerId);
        when(digimon.getDigimonInfoId()).thenReturn(7L);
        when(digimon.getRarity()).thenReturn(Rarity.COMMON);
        when(collectionRepository.existsByPlayerIdAndDigimonInfoIdAndRarity(playerId, 7L, Rarity.COMMON))
                .thenReturn(true);

        CollectionRegistrationService service = new CollectionRegistrationService(
                collectionRepository,
                digimonRepository,
                addItemUseCase
        );

        assertFalse(service.registerIfMissing(digimon, "EVOLUTION"));
        verify(collectionRepository, never()).save(any(CollectionEntry.class));
    }

    @Test
    void retroactiveSyncUsesOnlyCurrentlyOwnedDigimons() {
        UUID playerId = UUID.randomUUID();
        Digimon active = digimon(playerId, 1L, Rarity.COMMON, DigimonStatus.ACTIVE);
        Digimon stored = digimon(playerId, 2L, Rarity.RARE, DigimonStatus.STORED);
        Digimon sacrificed = digimon(playerId, 3L, Rarity.EPIC, DigimonStatus.SACRIFICED);
        when(digimonRepository.findByPlayerId(playerId)).thenReturn(List.of(active, stored, sacrificed));
        when(collectionRepository.existsByPlayerIdAndDigimonInfoIdAndRarity(any(), any(), any())).thenReturn(false);
        when(collectionRepository.countByPlayer(playerId)).thenReturn(0L, 1L);

        CollectionRegistrationService service = new CollectionRegistrationService(
                collectionRepository,
                digimonRepository,
                addItemUseCase
        );

        assertEquals(2, service.syncOwnedDigimons(playerId));
        verify(collectionRepository, org.mockito.Mockito.times(2)).save(any(CollectionEntry.class));
    }

    private Digimon digimon(UUID playerId, Long infoId, Rarity rarity, DigimonStatus status) {
        Digimon digimon = org.mockito.Mockito.mock(Digimon.class);
        when(digimon.getPlayerId()).thenReturn(playerId);
        when(digimon.getId()).thenReturn(UUID.randomUUID());
        when(digimon.getDigimonInfoId()).thenReturn(infoId);
        when(digimon.getRarity()).thenReturn(rarity);
        when(digimon.getStatus()).thenReturn(status);
        return digimon;
    }
}
