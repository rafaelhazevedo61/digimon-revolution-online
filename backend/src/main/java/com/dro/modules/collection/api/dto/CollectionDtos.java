package com.dro.modules.collection.api.dto;

import com.dro.modules.collection.domain.CollectionEntry;
import com.dro.modules.digimon.domain.enums.Rarity;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class CollectionDtos {
    private CollectionDtos() {}
    public record RegisterRequest(@NotNull UUID digimonId) {}
    public record EntryResponse(UUID id, Long digimonInfoId, Rarity rarity, UUID sourceDigimonId, LocalDateTime discoveredAt) {
        public static EntryResponse from(CollectionEntry e) { return new EntryResponse(e.getId(), e.getDigimonInfoId(), e.getRarity(), e.getSourceDigimonId(), e.getDiscoveredAt()); }
    }
    public record SummaryResponse(long points, long distinctRarities, List<EntryResponse> entries, List<Integer> availableMilestones) {}
    public record RegisterResponse(EntryResponse entry, long points, boolean speciesMasteryUnlocked, String message) {}
}
