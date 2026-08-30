package com.dro.modules.ranking.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.ranking.api.dto.response.RankingEntryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Ranking.
 */
@Service
public class GetRankingUseCase {
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final DigimonInfosRepository digimonInfosRepository;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;
    private static final List<DigimonStatus> RANKING_STATUSES = List.of(DigimonStatus.ACTIVE, DigimonStatus.STORED);

    public List<RankingEntryResponse> byLevel(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = sanitizeSize(size);
        Page<Digimon> result = digimonRepository.findByStatusInOrderByLevelDescExperienceDesc(RANKING_STATUSES, PageRequest.of(safePage, safeSize));
        return toResponse(result, safePage, safeSize);
    }

    public List<RankingEntryResponse> byGrade(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = sanitizeSize(size);
        Page<Digimon> result = digimonRepository.findByStatusInOrderByGradeQualityAscLevelDesc(RANKING_STATUSES, PageRequest.of(safePage, safeSize));
        return toResponse(result, safePage, safeSize);
    }

    public List<RankingEntryResponse> byRebirth(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = sanitizeSize(size);
        Page<Digimon> result = digimonRepository.findByStatusInAndRebirthCountGreaterThanOrderByRebirthCountDescLevelDesc(RANKING_STATUSES, 0, PageRequest.of(safePage, safeSize));
        return toResponse(result, safePage, safeSize);
    }

    private int sanitizeSize(int size) {
        if (size <= 0) return DEFAULT_SIZE;
        return Math.min(size, MAX_SIZE);
    }

    private List<RankingEntryResponse> toResponse(Page<Digimon> page, int pageNumber, int pageSize) {
        List<Digimon> digimons = page.getContent();
        List<UUID> playerIds = digimons.stream().map(Digimon::getPlayerId).distinct().toList();
        Map<UUID, String> playerNames = playerRepository.findAllById(playerIds).stream().collect(Collectors.toMap(Player::getId, Player::getUsername));
        List<Long> digimonInfoIds = digimons.stream().map(Digimon::getDigimonInfoId).filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, String> imageUrls = digimonInfosRepository.findAllById(digimonInfoIds).stream().collect(Collectors.toMap(DigimonInfos::getId, DigimonInfos::getImageUrl));
        List<RankingEntryResponse> entries = new java.util.ArrayList<>();
        for (int i = 0; i < digimons.size(); i++) {
            Digimon d = digimons.get(i);
            int position = pageNumber * pageSize + i + 1;
            entries.add(new RankingEntryResponse(position, d.getName(), d.getStage(), imageUrls.get(d.getDigimonInfoId()), d.getLevel(), d.getGrade(), d.getRebirthCount(), playerNames.getOrDefault(d.getPlayerId(), "Unknown"), d.getId(), d.getPlayerId()));
        }
        return entries;
    }

    public GetRankingUseCase(final DigimonRepository digimonRepository, final PlayerRepository playerRepository, final DigimonInfosRepository digimonInfosRepository) {
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
        this.digimonInfosRepository = digimonInfosRepository;
    }
}
