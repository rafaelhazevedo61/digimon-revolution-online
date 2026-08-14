package com.dro.modules.clan.application;

import com.dro.modules.clan.api.dto.response.BuySlotResponse;
import com.dro.modules.clan.domain.Clan;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuySlotUseCase {

    private final ClanAuthorizationService authorization;
    private final ClanRepository clanRepository;
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;

    @Transactional
    public BuySlotResponse execute(String token, UUID clanId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = authorization.getPlayer(playerId);
        Clan clan = authorization.getClan(clanId);

        authorization.assertCanBuySlot(player, clan);

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("You need an active Digimon to buy a clan slot");
        }
        Digimon activeDigimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active Digimon not found"));

        if (ClanRules.isMaxSlotsReached(clan.getBoughtSlots())) {
            throw new BadRequestException("Maximum bought slots reached (" + ClanRules.MAX_BOUGHT_SLOTS + ")");
        }

        int cost = ClanRules.slotCost(clan.getBoughtSlots());
        if (activeDigimon.getBits() < cost) {
            throw new BadRequestException("Not enough bits to buy a clan slot");
        }

        activeDigimon.setBits(activeDigimon.getBits() - cost);
        clan.setBoughtSlots(clan.getBoughtSlots() + 1);

        digimonRepository.save(activeDigimon);
        clanRepository.save(clan);

        return new BuySlotResponse(
                clan.getBoughtSlots(),
                clan.getEffectiveMaxMembers(),
                activeDigimon.getBits(),
                ClanRules.slotCost(clan.getBoughtSlots())
        );
    }
}
