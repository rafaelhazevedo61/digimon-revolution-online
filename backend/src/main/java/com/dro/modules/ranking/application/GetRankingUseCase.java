package com.dro.modules.ranking.application;

import com.dro.modules.arena.application.DigimonPowerService;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
    private final DigimonPowerService digimonPowerService;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;
    private static final List<DigimonStatus> RANKING_STATUSES = List.of(DigimonStatus.ACTIVE, DigimonStatus.STORED);

    public GetRankingUseCase(final DigimonRepository digimonRepository, final PlayerRepository playerRepository, final DigimonInfosRepository digimonInfosRepository, final DigimonPowerService digimonPowerService) {
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
        this.digimonInfosRepository = digimonInfosRepository;
        this.digimonPowerService = digimonPowerService;
    }

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

    public List<RankingEntryResponse> byPower(int page, int size, String search) {
        int safePage = Math.max(page, 0);
        int safeSize = sanitizeSize(size);

        List<Digimon> digimons = digimonRepository.findByStatusInAndBotFalse(RANKING_STATUSES);
        Map<UUID, Double> powers = new HashMap<>();
        for (Digimon d : digimons) {
            powers.put(d.getId(), digimonPowerService.calculatePower(d));
        }

        digimons.sort(Comparator
                .comparingDouble((Digimon d) -> powers.getOrDefault(d.getId(), 0.0)).reversed()
                .thenComparingInt(Digimon::getLevel).reversed()
                .thenComparing(Digimon::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

        List<RankingEntryResponse> entries = buildEntries(digimons, powers);
        return paginate(filterBySearch(entries, search), safePage, safeSize);
    }

    private List<RankingEntryResponse> filterBySearch(List<RankingEntryResponse> entries, String search) {
        if (search == null || search.isBlank()) {
            return entries;
        }
        String term = search.trim().toLowerCase(Locale.ROOT);
        return entries.stream()
                .filter(e -> e.digimonName().toLowerCase(Locale.ROOT).contains(term)
                        || e.playerName().toLowerCase(Locale.ROOT).contains(term))
                .toList();
    }

    private List<RankingEntryResponse> paginate(List<RankingEntryResponse> entries, int page, int size) {
        int start = page * size;
        if (start >= entries.size()) {
            return List.of();
        }
        int end = Math.min(start + size, entries.size());
        List<RankingEntryResponse> pageEntries = new ArrayList<>(end - start);
        for (int i = start; i < end; i++) {
            RankingEntryResponse e = entries.get(i);
            pageEntries.add(new RankingEntryResponse(
                    i + 1,
                    e.digimonName(),
                    e.digimonStage(),
                    e.imageUrl(),
                    e.level(),
                    e.grade(),
                    e.rebirthCount(),
                    e.playerName(),
                    e.digimonId(),
                    e.playerId(),
                    e.power()
            ));
        }
        return pageEntries;
    }

    private List<RankingEntryResponse> buildEntries(List<Digimon> digimons, Map<UUID, Double> powers) {
        List<UUID> playerIds = digimons.stream().map(Digimon::getPlayerId).distinct().toList();
        Map<UUID, String> playerNames = playerRepository.findAllById(playerIds).stream().collect(Collectors.toMap(Player::getId, Player::getUsername));
        List<Long> digimonInfoIds = digimons.stream().map(Digimon::getDigimonInfoId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> imageUrls = digimonInfosRepository.findAllById(digimonInfoIds).stream().collect(Collectors.toMap(DigimonInfos::getId, DigimonInfos::getImageUrl));

        List<RankingEntryResponse> entries = new ArrayList<>(digimons.size());
        int position = 1;
        for (Digimon d : digimons) {
            long power = powers != null ? Math.round(powers.getOrDefault(d.getId(), 0.0)) : 0L;
            entries.add(new RankingEntryResponse(
                    position++,
                    d.getName(),
                    d.getStage(),
                    imageUrls.get(d.getDigimonInfoId()),
                    d.getLevel(),
                    d.getGrade(),
                    d.getRebirthCount(),
                    playerNames.getOrDefault(d.getPlayerId(), "Unknown"),
                    d.getId(),
                    d.getPlayerId(),
                    power
            ));
        }
        return entries;
    }

    private int sanitizeSize(int size) {
        if (size <= 0) return DEFAULT_SIZE;
        return Math.min(size, MAX_SIZE);
    }

    private List<RankingEntryResponse> toResponse(Page<Digimon> page, int pageNumber, int pageSize) {
        List<Digimon> digimons = page.getContent();
        List<UUID> playerIds = digimons.stream().map(Digimon::getPlayerId).distinct().toList();
        Map<UUID, String> playerNames = playerRepository.findAllById(playerIds).stream().collect(Collectors.toMap(Player::getId, Player::getUsername));
        List<Long> digimonInfoIds = digimons.stream().map(Digimon::getDigimonInfoId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> imageUrls = digimonInfosRepository.findAllById(digimonInfoIds).stream().collect(Collectors.toMap(DigimonInfos::getId, DigimonInfos::getImageUrl));
        List<RankingEntryResponse> entries = new ArrayList<>();
        for (int i = 0; i < digimons.size(); i++) {
            Digimon d = digimons.get(i);
            int position = pageNumber * pageSize + i + 1;
            entries.add(new RankingEntryResponse(position, d.getName(), d.getStage(), imageUrls.get(d.getDigimonInfoId()), d.getLevel(), d.getGrade(), d.getRebirthCount(), playerNames.getOrDefault(d.getPlayerId(), "Unknown"), d.getId(), d.getPlayerId(), 0L));
        }
        return entries;
    }
}
