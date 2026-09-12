package com.dro.modules.collection.application;

import com.dro.modules.collection.infra.CollectionEntryRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.Rarity;
import com.dro.modules.inventory.application.AddItemUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionRegistrationServiceTest {
    @Mock
    private CollectionEntryRepository collectionRepository;
    @Mock
    private AddItemUseCase addItemUseCase;

    @Test
    void registersSpeciesAndRarityWithIdempotentInsert() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Digimon digimon = org.mockito.Mockito.mock(Digimon.class);
        when(digimon.getPlayerId()).thenReturn(playerId);
        when(digimon.getId()).thenReturn(digimonId);
        when(digimon.getDigimonInfoId()).thenReturn(42L);
        when(digimon.getRarity()).thenReturn(Rarity.RARE);
        when(collectionRepository.countByPlayer(playerId)).thenReturn(3L);
        when(collectionRepository.insertIfMissing(
                any(UUID.class), eq(playerId), eq(42L), eq("RARE"), eq(digimonId), eq("HATCH")
        )).thenReturn(1);

        CollectionRegistrationService service = new CollectionRegistrationService(collectionRepository, addItemUseCase);

        assertTrue(service.registerIfMissing(digimon, "HATCH"));
        verify(collectionRepository).insertIfMissing(
                any(UUID.class), eq(playerId), eq(42L), eq("RARE"), eq(digimonId), eq("HATCH")
        );
        verify(collectionRepository, never()).save(any());
    }

    @Test
    void ignoresAlreadyRegisteredCombinationFromConflictResult() {
        UUID playerId = UUID.randomUUID();
        UUID digimonId = UUID.randomUUID();
        Digimon digimon = org.mockito.Mockito.mock(Digimon.class);
        when(digimon.getPlayerId()).thenReturn(playerId);
        when(digimon.getId()).thenReturn(digimonId);
        when(digimon.getDigimonInfoId()).thenReturn(7L);
        when(digimon.getRarity()).thenReturn(Rarity.COMMON);
        when(collectionRepository.countByPlayer(playerId)).thenReturn(3L);
        when(collectionRepository.insertIfMissing(
                any(UUID.class), eq(playerId), eq(7L), eq("COMMON"), eq(digimonId), eq("EVOLUTION")
        )).thenReturn(0);

        CollectionRegistrationService service = new CollectionRegistrationService(collectionRepository, addItemUseCase);

        assertFalse(service.registerIfMissing(digimon, "EVOLUTION"));
        verify(collectionRepository).insertIfMissing(
                any(UUID.class), eq(playerId), eq(7L), eq("COMMON"), eq(digimonId), eq("EVOLUTION")
        );
        verify(collectionRepository, never()).save(any());
    }

    @Test
    void retroactiveSyncInsertsOwnedEntriesInOneBatch() {
        UUID playerId = UUID.randomUUID();
        when(collectionRepository.countByPlayer(playerId)).thenReturn(0L);
        when(collectionRepository.insertMissingOwnedEntries(playerId)).thenReturn(2);

        CollectionRegistrationService service = new CollectionRegistrationService(collectionRepository, addItemUseCase);

        assertEquals(2, service.syncOwnedDigimons(playerId));
        verify(collectionRepository).insertMissingOwnedEntries(playerId);
        verify(collectionRepository, never()).existsByPlayerIdAndDigimonInfoIdAndRarity(any(), any(), any());
    }
}
