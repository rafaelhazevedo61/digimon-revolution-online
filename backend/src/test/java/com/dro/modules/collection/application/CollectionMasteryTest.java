package com.dro.modules.collection.application;

import com.dro.modules.collection.infra.CollectionEntryRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionMasteryTest {
    @Mock
    private CollectionEntryRepository collectionRepository;
    @Mock
    private AddItemUseCase addItemUseCase;

    @Test
    void unlocksOnlyAfterAllFourRaritiesAreRegistered() {
        UUID playerId = UUID.randomUUID();
        when(collectionRepository.countRaritiesForSpecies(playerId, 42L)).thenReturn(4L);

        CollectionRegistrationService service = new CollectionRegistrationService(
                collectionRepository,
                org.mockito.Mockito.mock(com.dro.modules.digimon.infra.DigimonRepository.class),
                addItemUseCase
        );

        assertTrue(service.isSpeciesMasteryUnlocked(playerId, 42L));
    }

    @Test
    void remainsLockedWhenAnyRarityIsMissing() {
        UUID playerId = UUID.randomUUID();
        when(collectionRepository.countRaritiesForSpecies(playerId, 42L)).thenReturn(3L);

        CollectionRegistrationService service = new CollectionRegistrationService(
                collectionRepository,
                org.mockito.Mockito.mock(com.dro.modules.digimon.infra.DigimonRepository.class),
                addItemUseCase
        );

        assertFalse(service.isSpeciesMasteryUnlocked(playerId, 42L));
    }
}
