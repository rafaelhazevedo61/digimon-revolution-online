package com.dro.modules.collection.application;

import com.dro.modules.collection.api.dto.CollectionDtos;
import com.dro.modules.collection.domain.CollectionEntry;
import com.dro.modules.collection.infra.CollectionEntryRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class CollectionUseCase {
    private static final int[] MILESTONES = {10, 50, 100, 150, 200, 250, 300};

    private final CollectionEntryRepository collectionRepository;
    private final DigimonRepository digimonRepository;
    private final DigimonInfosRepository digimonInfosRepository;
    private final CollectionRegistrationService registrationService;

    public CollectionUseCase(
            CollectionEntryRepository collectionRepository,
            DigimonRepository digimonRepository,
            DigimonInfosRepository digimonInfosRepository,
            CollectionRegistrationService registrationService
    ) {
        this.collectionRepository = collectionRepository;
        this.digimonRepository = digimonRepository;
        this.digimonInfosRepository = digimonInfosRepository;
        this.registrationService = registrationService;
    }

    @Transactional
    public CollectionDtos.SummaryResponse summary(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);

        List<CollectionDtos.EntryResponse> entries = collectionRepository
                .findByPlayerIdOrderByDiscoveredAtDesc(playerId)
                .stream()
                .map(entry -> CollectionDtos.EntryResponse.from(
                        entry,
                        digimonInfosRepository.findById(entry.getDigimonInfoId())
                                .map(info -> info.getName())
                                .orElse("Digimon")
                ))
                .toList();

        long points = entries.size();
        List<Integer> available = Arrays.stream(MILESTONES)
                .filter(milestone -> points >= milestone)
                .boxed()
                .toList();
        List<CollectionDtos.MilestoneResponse> milestones = Arrays.stream(MILESTONES)
                .mapToObj(milestone -> new CollectionDtos.MilestoneResponse(
                        milestone,
                        "1 Disco de XP de 20%",
                        points >= milestone
                ))
                .toList();
        long totalDigimons = digimonInfosRepository.count();

        return new CollectionDtos.SummaryResponse(
                points,
                collectionRepository.countDistinctRarities(playerId),
                collectionRepository.countAddedDigimons(playerId),
                totalDigimons,
                collectionRepository.countCompletedDigimons(playerId),
                entries,
                available,
                milestones
        );
    }

    /**
     * Mantido por compatibilidade com clientes antigos. O registro agora é não destrutivo:
     * não consome Digimon nem Digivice e é idempotente.
     */
    @Transactional
    public CollectionDtos.RegisterResponse register(String token, UUID digimonId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Digimon digimon = digimonRepository.findById(digimonId)
                .orElseThrow(() -> new NotFoundException("Digimon not found"));
        if (!playerId.equals(digimon.getPlayerId())) {
            throw new BadRequestException("Este Digimon não pertence ao jogador");
        }

        boolean created = registrationService.registerIfMissing(digimon, "LEGACY_MANUAL_REGISTRATION");
        CollectionEntry entry = collectionRepository
                .findByPlayerIdOrderByDiscoveredAtDesc(playerId)
                .stream()
                .filter(candidate -> candidate.getDigimonInfoId().equals(digimon.getDigimonInfoId())
                        && candidate.getRarity() == digimon.getRarity())
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Collection entry not found"));

        long points = collectionRepository.countByPlayer(playerId);
        boolean mastery = collectionRepository.countRaritiesForSpecies(playerId, digimon.getDigimonInfoId()) == 4;
        String speciesName = digimonInfosRepository.findById(entry.getDigimonInfoId())
                .map(info -> info.getName())
                .orElse("Digimon");
        String message = created
                ? "Digimon registrado automaticamente na coleção"
                : "Esta espécie e raridade já estavam registradas";

        return new CollectionDtos.RegisterResponse(
                CollectionDtos.EntryResponse.from(entry, speciesName),
                points,
                mastery,
                message
        );
    }
}
