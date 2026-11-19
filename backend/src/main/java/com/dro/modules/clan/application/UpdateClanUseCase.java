package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.ClanResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanRules;
import com.dro.modules.player.domain.Player;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Clãs.
 */
@Service
public class UpdateClanUseCase {
    private final ClanAuthorizationService authorization;
    private final ClanResponseMapper mapper;

    @Transactional
    public ClanResponse execute(String token, UUID clanId, String description, String emblem) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = authorization.getPlayer(playerId);
        Clan clan = authorization.getClan(clanId);
        authorization.assertCanManageInfo(player, clan);
        if (description != null) {
            if (!ClanRules.isDescriptionValid(description)) {
                throw new BadRequestException("Description is too long");
            }
            clan.setDescription(description.trim());
        }
        if (emblem != null) {
            if (emblem.length() > 50) {
                throw new BadRequestException("Emblem code is too long");
            }
            clan.setEmblem(emblem.trim().isEmpty() ? null : emblem.trim());
        }
        return mapper.toResponse(clan, player, authorization.getMembers(clanId));
    }

    public UpdateClanUseCase(final ClanAuthorizationService authorization, final ClanResponseMapper mapper) {
        this.authorization = authorization;
        this.mapper = mapper;
    }
}
