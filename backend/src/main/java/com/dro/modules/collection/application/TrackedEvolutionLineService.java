package com.dro.modules.collection.application;

import com.dro.modules.evolution.domain.EvolutionLine;
import com.dro.modules.evolution.infra.EvolutionLineRepository;
import com.dro.modules.player.application.VipAccess;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class TrackedEvolutionLineService {
    private final PlayerRepository players;
    private final VipAccess vipAccess;
    private final EvolutionLineRepository lines;

    public TrackedEvolutionLineService(PlayerRepository players, VipAccess vipAccess,
                                       EvolutionLineRepository lines) {
        this.players = players;
        this.vipAccess = vipAccess;
        this.lines = lines;
    }

    @Transactional(readOnly = true)
    public Status get(UUID playerId) {
        Player player = players.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Jogador não encontrado."));
        if (!player.hasActiveVip(Instant.now())) {
            return new Status(false, null);
        }
        Long lineId = player.getTrackedEvolutionLineId();
        String code = lineId == null ? null : lines.findByIdAndActiveTrueAndContentActiveTrue(lineId)
                .map(EvolutionLine::getCode)
                .orElse(null);
        return new Status(true, code);
    }

    @Transactional
    public Status update(UUID playerId, String code) {
        Player player = vipAccess.lockPlayer(playerId);
        if (!player.hasActiveVip(Instant.now())) {
            throw new ForbiddenException("Acompanhar linhas evolutivas exige VIP ativo.");
        }
        EvolutionLine line = code == null ? null : lines.findByCodeAndActiveTrueAndContentActiveTrue(code)
                .orElseThrow(() -> new NotFoundException("Linha evolutiva indisponível."));
        player.setTrackedEvolutionLineId(line == null ? null : line.getId());
        return new Status(true, line == null ? null : line.getCode());
    }

    public record Status(boolean vipActive, String lineCode) {}
}
