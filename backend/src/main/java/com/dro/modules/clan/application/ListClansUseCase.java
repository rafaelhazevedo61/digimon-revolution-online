package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanSummaryResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.player.infra.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListClansUseCase {

    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;
    private final ClanResponseMapper mapper;

    public Page<ClanSummaryResponse> execute(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Clan> clans;
        if (query != null && !query.isBlank()) {
            clans = clanRepository.searchByNameOrTag(query.trim(), pageable);
        } else {
            clans = clanRepository.findAll(pageable);
        }
        return clans.map(c -> mapper.toSummary(c, (int) playerRepository.countByClanId(c.getId())));
    }
}
