package com.dro.modules.collection.application;

import com.dro.modules.collection.domain.CollectionEntry;
import com.dro.modules.collection.infra.CollectionEntryRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
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
    private final DigimonRepository digimonRepository;
    private final AddItemUseCase addItemUseCase;

    public CollectionRegistrationService(
            CollectionEntryRepository collectionRepository,
            DigimonRepository digimonRepository,
            AddItemUseCase addItemUseCase
    ) {
        this.collectionRepository = collectionRepository;
        this.digimonRepository = digimonRepository;
        this.addItemUseCase = addItemUseCase;
    }

    @Transactional
    public boolean registerIfMissing(Digimon digimon, String sourceEvent) {
        if (digimon == null || digimon.getPlayerId() == null || digimon.getDigimonInfoId() == null || digimon.getRarity() == null) {
            return false;
        }

        UUID playerId = digimon.getPlayerId();
        if (collectionRepository.existsByPlayerIdAndDigimonInfoIdAndRarity(
                playerId,
                digimon.getDigimonInfoId(),
                digimon.getRarity()
        )) {
            return false;
        }

        long before = collectionRepository.countByPlayer(playerId);
        collectionRepository.save(new CollectionEntry(
                UUID.randomUUID(),
                playerId,
                digimon.getDigimonInfoId(),
                digimon.getRarity(),
                digimon.getId(),
                sourceEvent
        ));

        long after = before + 1;
        int reachedMilestones = (int) Arrays.stream(MILESTONES)
                .filter(milestone -> before < milestone && after >= milestone)
                .count();
        if (reachedMilestones > 0) {
            addItemUseCase.execute(playerId, ItemType.XP_DISC_20, reachedMilestones);
        }
        return true;
    }

    @Transactional
    public int syncOwnedDigimons(UUID playerId) {
        int registered = 0;
        for (Digimon digimon : digimonRepository.findByPlayerId(playerId)) {
            if (registerIfMissing(digimon, "OWNED_DIGIMON_SYNC")) {
                registered++;
            }
        }
        return registered;
    }
}
