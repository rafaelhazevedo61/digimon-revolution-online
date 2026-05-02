package com.dro.modules.ranking.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.ranking.api.dto.response.RankingEntryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetRankingUseCase {

    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    public List<RankingEntryResponse> byLevel(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = sanitizeSize(size);
        Page<Digimon> result = digimonRepository
                .findByStatusOrderByLevelDescExperienceDesc(DigimonStatus.ACTIVE, PageRequest.of(safePage, safeSize));
        return toResponse(result, safePage, safeSize);
    }

    public List<RankingEntryResponse> byGrade(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = sanitizeSize(size);
        Page<Digimon> result = digimonRepository
                .findByStatusOrderByGradeQualityAscLevelDesc(DigimonStatus.ACTIVE, PageRequest.of(safePage, safeSize));
        return toResponse(result, safePage, safeSize);
    }

    public List<RankingEntryResponse> byRebirth(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = sanitizeSize(size);
        Page<Digimon> result = digimonRepository
                .findByStatusAndRebirthCountGreaterThanOrderByRebirthCountDescLevelDesc(DigimonStatus.ACTIVE, 0, PageRequest.of(safePage, safeSize));
        return toResponse(result, safePage, safeSize);
    }

    private int sanitizeSize(int size) {
        if (size <= 0) return DEFAULT_SIZE;
        return Math.min(size, MAX_SIZE);
    }

    private List<RankingEntryResponse> toResponse(Page<Digimon> page, int pageNumber, int pageSize) {
        List<Digimon> digimons = page.getContent();

        List<UUID> playerIds = digimons.stream()
                .map(Digimon::getPlayerId)
                .distinct()
                .toList();

        Map<UUID, String> playerNames = playerRepository.findAllById(playerIds)
                .stream()
                .collect(Collectors.toMap(Player::getId, Player::getUsername));

        List<RankingEntryResponse> entries = new java.util.ArrayList<>();

        for (int i = 0; i < digimons.size(); i++) {
            Digimon d = digimons.get(i);
            int position = pageNumber * pageSize + i + 1;

            entries.add(new RankingEntryResponse(
                    position,
                    d.getName(),
                    d.getStage(),
                    d.getLevel(),
                    d.getGrade(),
                    d.getRebirthCount(),
                    playerNames.getOrDefault(d.getPlayerId(), "Unknown"),
                    d.getId(),
                    d.getPlayerId()
            ));
        }

        return entries;
    }
}
