package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanRankingEntryResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetClanRankingUseCase {

    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;
    private final ClanResponseMapper mapper;

    public Page<ClanRankingEntryResponse> execute(int page, int size) {
        List<Clan> all = clanRepository.findAll(Sort.by("createdAt").descending());

        List<ClanRankingEntryResponse> ranked = all.stream()
                .map(c -> {
                    List<Player> members = playerRepository.findByClanId(c.getId());
                    return mapper.toRankingEntry(0, c, members);
                })
                .sorted(Comparator.comparingLong(ClanRankingEntryResponse::totalPower).reversed()
                        .thenComparing(ClanRankingEntryResponse::memberCount).reversed())
                .toList();

        List<ClanRankingEntryResponse> withPosition = ranked.stream()
                .map(entry -> new ClanRankingEntryResponse(
                        ranked.indexOf(entry) + 1,
                        entry.id(),
                        entry.name(),
                        entry.tag(),
                        entry.memberCount(),
                        entry.totalPower()
                ))
                .toList();

        int start = page * size;
        if (start >= withPosition.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), withPosition.size());
        }
        int end = Math.min(start + size, withPosition.size());
        return new PageImpl<>(withPosition.subList(start, end), PageRequest.of(page, size), withPosition.size());
    }
}
