package com.dro.modules.collection.application;

import com.dro.modules.collection.infra.CollectionEntryRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.domain.ItemType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.UUID;

@Service
public class CollectionRegistrationService {
    private static final int[] MILESTONES = {10, 50, 100, 150, 200, 250, 300};

    private final CollectionEntryRepository collectionRepository;
    private final AddItemUseCase addItemUseCase;

    public CollectionRegistrationService(
            CollectionEntryRepository collectionRepository,
            AddItemUseCase addItemUseCase
    ) {
        this.collectionRepository = collectionRepository;
        this.addItemUseCase = addItemUseCase;
    }

    @Transactional
    public boolean registerIfMissing(Digimon digimon, String sourceEvent) {
        if (digimon == null || digimon.getPlayerId() == null || digimon.getDigimonInfoId() == null || digimon.getRarity() == null) {
            return false;
        }

        UUID playerId = digimon.getPlayerId();
        long before = collectionRepository.countByPlayer(playerId);
        int inserted = collectionRepository.insertIfMissing(
                UUID.randomUUID(),
                playerId,
                digimon.getDigimonInfoId(),
                digimon.getRarity().name(),
                digimon.getId(),
                sourceEvent
        );
        if (inserted == 0) {
            return false;
        }

        long after = before + 1;
        int reachedMilestones = (int) Arrays.stream(MILESTONES)
                .filter(milestone -> before < milestone && after >= milestone)
                .count();
        if (reachedMilestones > 0) {
            addItemUseCase.tryExecute(playerId, ItemType.XP_DISC_20, reachedMilestones);
        }
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isSpeciesMasteryUnlocked(UUID playerId, Long digimonInfoId) {
        return playerId != null
                && digimonInfoId != null
                && collectionRepository.countRaritiesForSpecies(playerId, digimonInfoId) == 4;
    }

    @Transactional
    public int syncOwnedDigimons(UUID playerId) {
        long before = collectionRepository.countByPlayer(playerId);
        int registered = collectionRepository.insertMissingOwnedEntries(playerId);
        long after = before + registered;
        int reachedMilestones = (int) Arrays.stream(MILESTONES)
                .filter(milestone -> before < milestone && after >= milestone)
                .count();
        if (reachedMilestones > 0) {
            addItemUseCase.tryExecute(playerId, ItemType.XP_DISC_20, reachedMilestones);
        }
        return registered;
    }
}
