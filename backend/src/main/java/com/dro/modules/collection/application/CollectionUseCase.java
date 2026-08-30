package com.dro.modules.collection.application;

import com.dro.modules.collection.api.dto.CollectionDtos;
import com.dro.modules.collection.domain.CollectionEntry;
import com.dro.modules.collection.infra.CollectionEntryRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class CollectionUseCase {
    private static final int[] MILESTONES = {10, 50, 100, 150, 200};
    private final CollectionEntryRepository collectionRepository;
    private final DigimonRepository digimonRepository;
    private final DigimonInfosRepository digimonInfosRepository;
    private final InventoryRepository inventoryRepository;
    private final AddItemUseCase addItemUseCase;

    @Transactional(readOnly = true)
    public CollectionDtos.SummaryResponse summary(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        List<CollectionDtos.EntryResponse> entries = collectionRepository.findByPlayerIdOrderByDiscoveredAtDesc(playerId).stream().map(entry -> CollectionDtos.EntryResponse.from(entry, digimonInfosRepository.findById(entry.getDigimonInfoId()).map(info -> info.getName()).orElse("Digimon"))).toList();
        long points = entries.size();
        List<Integer> available = java.util.Arrays.stream(MILESTONES).filter(m -> points >= m).boxed().toList();
        List<CollectionDtos.MilestoneResponse> milestones = java.util.Arrays.stream(MILESTONES)
                .mapToObj(m -> new CollectionDtos.MilestoneResponse(m, "1 Disco de XP de 20%", points >= m))
                .toList();
        long totalDigimons = digimonInfosRepository.count();
        return new CollectionDtos.SummaryResponse(points, collectionRepository.countDistinctRarities(playerId), collectionRepository.countAddedDigimons(playerId), totalDigimons, collectionRepository.countCompletedDigimons(playerId), entries, available, milestones);
    }

    @Transactional
    public CollectionDtos.RegisterResponse register(String token, UUID digimonId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Digimon digimon = digimonRepository.findByIdForUpdate(digimonId).orElseThrow(() -> new NotFoundException("Digimon not found"));
        if (!playerId.equals(digimon.getPlayerId())) throw new BadRequestException("Este Digimon não pertence ao jogador");
        if (digimon.getStatus() != DigimonStatus.STORED && digimon.getStatus() != DigimonStatus.HATCHED) throw new BadRequestException("Somente Digimons armazenados ou recém-nascidos podem ser registrados");
        if (digimon.isLocked()) throw new BadRequestException("Digimons bloqueados não podem ser registrados");
        if (digimon.getWeaponId() != null || digimon.getArmorId() != null || digimon.getAccessoryId() != null) throw new BadRequestException("Remova os equipamentos antes de registrar este Digimon");
        if (collectionRepository.existsByPlayerIdAndDigimonInfoIdAndRarity(playerId, digimon.getDigimonInfoId(), digimon.getRarity())) throw new BadRequestException("Esta espécie e raridade já estão na coleção");
        InventoryItem digivice = inventoryRepository.findByPlayerIdAndItemTypeForUpdate(playerId, ItemType.COLLECTION_DIGIVICE).orElseThrow(() -> new UnprocessableException("Você não possui Digivice de Registro"));
        if (digivice.getQuantity() < 1) throw new UnprocessableException("Você não possui Digivice de Registro");
        digivice.setQuantity(digivice.getQuantity() - 1);
        if (digivice.getQuantity() == 0) inventoryRepository.delete(digivice); else inventoryRepository.save(digivice);
        long before = collectionRepository.countByPlayer(playerId);
        digimon.setStatus(DigimonStatus.COLLECTION_CONSUMED);
        digimonRepository.save(digimon);
        CollectionEntry entry = collectionRepository.save(new CollectionEntry(UUID.randomUUID(), playerId, digimon.getDigimonInfoId(), digimon.getRarity(), digimon.getId(), "MANUAL_REGISTRATION"));
        long after = before + 1;
        int milestoneRewards = (int) java.util.Arrays.stream(MILESTONES).filter(m -> before < m && after >= m).count();
        if (milestoneRewards > 0) addItemUseCase.execute(playerId, ItemType.XP_DISC_20, milestoneRewards);
        boolean mastery = collectionRepository.countRaritiesForSpecies(playerId, digimon.getDigimonInfoId()) == 4;
        String message = milestoneRewards > 0 ? "Digimon registrado; recompensa de marco recebida" : "Digimon registrado na coleção";
        String speciesName = digimonInfosRepository.findById(entry.getDigimonInfoId()).map(info -> info.getName()).orElse("Digimon");
        return new CollectionDtos.RegisterResponse(CollectionDtos.EntryResponse.from(entry, speciesName), after, mastery, message);
    }

    public CollectionUseCase(CollectionEntryRepository collectionRepository, DigimonRepository digimonRepository, DigimonInfosRepository digimonInfosRepository, InventoryRepository inventoryRepository, AddItemUseCase addItemUseCase) {
        this.collectionRepository = collectionRepository; this.digimonRepository = digimonRepository; this.digimonInfosRepository = digimonInfosRepository; this.inventoryRepository = inventoryRepository; this.addItemUseCase = addItemUseCase;
    }
}
