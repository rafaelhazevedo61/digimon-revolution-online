package com.dro.modules.player.application;

import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.player.api.dto.response.AdminPlayerResponse;
import com.dro.modules.player.domain.VipPlan;
import com.dro.shared.audit.AdminAuditService;
import com.dro.shared.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SetPlayerVipUseCase {
    private final VipAccess vipAccess;
    private final MissionInstanceRepository missions;
    private final IncubationRepository incubations;
    private final AdminAuditService audit;

    public SetPlayerVipUseCase(VipAccess vipAccess, MissionInstanceRepository missions,
                               IncubationRepository incubations, AdminAuditService audit) {
        this.vipAccess = vipAccess;
        this.missions = missions;
        this.incubations = incubations;
        this.audit = audit;
    }

    @Transactional
    public AdminPlayerResponse extend(String authorization, UUID playerId, VipPlan plan) {
        var player = vipAccess.lockPlayer(playerId);
        Instant now = Instant.now();
        Instant base = player.hasActiveVip(now) ? player.getVipExpiresAt() : now;
        Instant expiresAt = base.plus(plan.getDays(), ChronoUnit.DAYS);
        player.setVipExpiresAt(expiresAt);
        audit.success(authorization, "ADMIN_PLAYER_VIP_UPDATED", "Player", playerId.toString(),
                "extend-vip", "Passe VIP aplicado",
                Map.of("targetPlayerId", playerId.toString(), "plan", plan.name(),
                        "expiresAt", expiresAt.toString()));
        return AdminPlayerResponse.from(player);
    }

    @Transactional
    public AdminPlayerResponse execute(String authorization, UUID playerId, Instant expiresAt) {
        var player = vipAccess.lockPlayer(playerId);
        Instant now = Instant.now();
        if (expiresAt != null && !expiresAt.isAfter(now)) {
            throw new BadRequestException("Informe uma data de validade futura.");
        }
        if (expiresAt == null || !player.hasActiveVip(now)) {
            player.setIncubationAutoClaimEnabled(false);
            player.setIncubationAutoRepeatEnabled(false);
            missions.findByPlayerIdAndStatusIn(playerId, List.of(MissionStatus.RUNNING, MissionStatus.COMPLETED))
                    .stream().filter(mission -> mission.isAutoClaimEnabled() || mission.isAutoRepeatEnabled())
                    .forEach(mission -> mission.pauseAutomation(VipAccess.PAUSE_REASON, VipAccess.PAUSE_REASON));
            incubations.findByPlayerIdAndStatusNotOrderBySlotNumberAsc(playerId, IncubationStatus.CLAIMED)
                    .stream().filter(incubation -> incubation.isAutoClaimEnabled() || incubation.isAutoRepeatEnabled())
                    .forEach(incubation -> incubation.pauseAutomation(VipAccess.PAUSE_REASON, VipAccess.PAUSE_REASON));
        }
        player.setVipExpiresAt(expiresAt);
        audit.success(authorization, "ADMIN_PLAYER_VIP_UPDATED", "Player", playerId.toString(),
                "update-vip", "Validade VIP atualizada",
                Map.of("targetPlayerId", playerId.toString(),
                        "expiresAt", expiresAt == null ? "revoked" : expiresAt.toString()));
        return AdminPlayerResponse.from(player);
    }
}
