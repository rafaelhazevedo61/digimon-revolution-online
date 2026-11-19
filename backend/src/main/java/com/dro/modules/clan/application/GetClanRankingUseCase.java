package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanRankingEntryResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Clãs.
 */
@Service
public class GetClanRankingUseCase {
    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;
    private final ClanResponseMapper mapper;

    public Page<ClanRankingEntryResponse> execute(int page, int size) {
        List<Clan> all = clanRepository.findByActiveTrue();
        List<ClanRankingEntryResponse> ranked = all.stream().map(c -> {
            List<Player> members = playerRepository.findByClanId(c.getId());
            return mapper.toRankingEntry(0, c, members);
        }).sorted((a, b) -> {
            int powerCmp = Long.compare(b.totalPower(), a.totalPower());
            if (powerCmp != 0) return powerCmp;
            int memberCmp = Integer.compare(b.memberCount(), a.memberCount());
            if (memberCmp != 0) return memberCmp;
            return a.name().compareToIgnoreCase(b.name());
        }).toList();
        List<ClanRankingEntryResponse> withPosition = new java.util.ArrayList<>();
        for (int i = 0; i < ranked.size(); i++) {
            ClanRankingEntryResponse entry = ranked.get(i);
            withPosition.add(new ClanRankingEntryResponse(i + 1, entry.id(), entry.name(), entry.tag(), entry.memberCount(), entry.totalPower()));
        }
        int start = page * size;
        if (start >= withPosition.size()) {
            return new PageImpl<>(List.of(), PageRequest.of(page, size), withPosition.size());
        }
        int end = Math.min(start + size, withPosition.size());
        return new PageImpl<>(withPosition.subList(start, end), PageRequest.of(page, size), withPosition.size());
    }

    public GetClanRankingUseCase(final ClanRepository clanRepository, final PlayerRepository playerRepository, final ClanResponseMapper mapper) {
        this.clanRepository = clanRepository;
        this.playerRepository = playerRepository;
        this.mapper = mapper;
    }
}
