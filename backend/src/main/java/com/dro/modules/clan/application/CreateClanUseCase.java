package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanRole;
import com.dro.modules.clan.domain.ClanRules;
import com.dro.modules.clan.infra.ClanRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateClanUseCase {

    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final ClanResponseMapper mapper;

    @Transactional
    public ClanResponse execute(String token, String name, String tag, String description) {
        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getClanId() != null) {
            throw new BadRequestException("You are already in a clan");
        }

        Digimon activeDigimon = null;
        if (ClanRules.CREATE_COST > 0) {
            if (player.getActiveDigimonId() == null) {
                throw new BadRequestException("You need an active Digimon to create a clan");
            }
            activeDigimon = digimonRepository.findById(player.getActiveDigimonId())
                    .orElseThrow(() -> new NotFoundException("Active Digimon not found"));
            if (activeDigimon.getBits() < ClanRules.CREATE_COST) {
                throw new BadRequestException("Not enough bits on your active Digimon to create a clan");
            }
            activeDigimon.setBits(activeDigimon.getBits() - ClanRules.CREATE_COST);
        }

        ClanRules.validateCreateRequest(name, tag, description);

        String normalizedTag = tag.toUpperCase().trim();
        String normalizedName = name.trim();

        if (clanRepository.existsByName(normalizedName)) {
            throw new BadRequestException("Clan name already taken");
        }
        if (clanRepository.existsByTag(normalizedTag)) {
            throw new BadRequestException("Clan tag already taken");
        }

        Clan clan = ClanRules.create(normalizedName, normalizedTag, description, playerId);
        clan = clanRepository.save(clan);

        player.setClanId(clan.getId());
        player.setClanRole(ClanRole.LEADER);
        player.setClanJoinedAt(LocalDateTime.now());
        playerRepository.save(player);

        if (activeDigimon != null) {
            digimonRepository.save(activeDigimon);
        }

        return mapper.toResponse(clan, player, Collections.singletonList(player));
    }
}
