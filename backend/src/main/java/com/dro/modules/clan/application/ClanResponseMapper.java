package com.dro.modules.clan.application;

import com.dro.modules.arena.application.DigimonPowerService;
import com.dro.modules.clan.api.dto.response.ClanMemberResponse;
import com.dro.modules.clan.api.dto.response.ClanRankingEntryResponse;
import com.dro.modules.clan.api.dto.response.ClanResponse;
import com.dro.modules.clan.api.dto.response.ClanRoleResponse;
import com.dro.modules.clan.api.dto.response.ClanSummaryResponse;
import com.dro.modules.clan.domain.Clan;
import com.dro.modules.clan.domain.ClanRules;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.player.domain.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClanResponseMapper {

    private final DigimonRepository digimonRepository;
    private final DigimonPowerService digimonPowerService;
    private final ClanBonusService clanBonusService;

    public ClanResponse toResponse(Clan clan, Player viewer, List<Player> members) {
        List<ClanMemberResponse> memberResponses = members.stream()
                .sorted(Comparator.comparing(ClanResponseMapper::roleOrder)
                        .thenComparing(Player::getUsername))
                .map(this::toMemberResponse)
                .toList();

        Player leader = members.stream()
                .filter(m -> m.getId().equals(clan.getLeaderId()))
                .findFirst()
                .orElse(viewer);

        boolean isMember = viewer.getClanId() != null && viewer.getClanId().equals(clan.getId());

        int memberCapacityUpgradeLevel = clanBonusService.getMemberCapacityBonus(clan.getId());
        int effectiveMaxMembers = clanBonusService.getEffectiveMaxMembers(clan);

        return new ClanResponse(
                clan.getId(),
                clan.getName(),
                clan.getTag(),
                clan.getDescription(),
                clan.getLeaderId(),
                leader.getUsername(),
                clan.getEmblem(),
                clan.getMaxMembers(),
                effectiveMaxMembers,
                memberCapacityUpgradeLevel,
                members.size(),
                clan.getLevel(),
                clan.getExperience(),
                ClanRules.xpToNextLevel(clan.getLevel(), clan.getExperience()),
                clan.getHonorMarks(),
                clan.getCreatedAt(),
                memberResponses,
                isMember,
                isMember ? new ClanRoleResponse(viewer.getClanRole()) : null,
                clanBonusService.activeUpgrades(clan.getId())
        );
    }

    private ClanMemberResponse toMemberResponse(Player member) {
        Integer power = null;
        if (member.getActiveDigimonId() != null) {
            Optional<Digimon> digimon = digimonRepository.findById(member.getActiveDigimonId());
            if (digimon.isPresent()) {
                power = (int) Math.round(digimonPowerService.calculatePower(digimon.get(), member.getClanId()));
            }
        }
        return new ClanMemberResponse(
                member.getId(),
                member.getUsername(),
                member.getClanRole(),
                member.getClanJoinedAt(),
                power
        );
    }

    public ClanSummaryResponse toSummary(Clan clan, int memberCount) {
        return new ClanSummaryResponse(
                clan.getId(),
                clan.getName(),
                clan.getTag(),
                clan.getDescription(),
                memberCount,
                clanBonusService.getEffectiveMaxMembers(clan),
                clan.getLevel(),
                clan.getCreatedAt()
        );
    }

    public ClanRankingEntryResponse toRankingEntry(int position, Clan clan, List<Player> members) {
        long totalPower = members.stream()
                .mapToLong(m -> activeDigimonPower(m.getActiveDigimonId(), m.getClanId()))
                .sum();
        return new ClanRankingEntryResponse(
                position,
                clan.getId(),
                clan.getName(),
                clan.getTag(),
                members.size(),
                totalPower
        );
    }

    private long activeDigimonPower(UUID activeDigimonId, UUID clanId) {
        if (activeDigimonId == null) return 0L;
        return digimonRepository.findById(activeDigimonId)
                .map(d -> (long) Math.round(digimonPowerService.calculatePower(d, clanId)))
                .orElse(0L);
    }

    private static int roleOrder(Player player) {
        if (player.getClanRole() == null) return 3;
        return switch (player.getClanRole()) {
            case LEADER -> 0;
            case OFFICER -> 1;
            case MEMBER -> 2;
        };
    }
}
