package com.dro.modules.tower.application;

import com.dro.modules.tower.api.TowerDtos.*;
import com.dro.modules.tower.domain.TowerRules;
import com.dro.modules.tower.domain.TowerRules.*;
import com.dro.modules.tower.infra.TowerRepository;
import com.dro.modules.tower.infra.TowerRepository.State;
import com.dro.modules.arena.application.DigimonPowerService;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.mission.infra.MissionTeamRepository;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.*;
import com.dro.shared.audit.TransactionAuditPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class TowerService {
    private final TowerRepository repository;
    private final PlayerRepository players;
    private final MissionTeamRepository teams;
    private final DigimonRepository digimons;
    private final MissionInstanceRepository missions;
    private final DigimonPowerService power;
    private final ItemDefinitionRepository items;
    private final AddItemUseCase grant;
    private final TransactionAuditPublisher audit;

    public TowerService(TowerRepository repository, PlayerRepository players, MissionTeamRepository teams,
                        DigimonRepository digimons, MissionInstanceRepository missions, DigimonPowerService power,
                        ItemDefinitionRepository items, AddItemUseCase grant, TransactionAuditPublisher audit) {
        this.repository = repository; this.players = players; this.teams = teams; this.digimons = digimons;
        this.missions = missions; this.power = power; this.items = items; this.grant = grant; this.audit = audit;
    }

    @Transactional(readOnly = true)
    public Page page(UUID playerId) {
        if (!players.existsById(playerId)) throw new NotFoundException("Jogador não encontrado");
        return page(repository.state(playerId), Instant.now());
    }

    private Page page(State state, Instant now) {
        return new Page(state.highestFloor(), state.attempts(), now, state.availableAt(),
                TowerRules.floor(Math.addExact(state.highestFloor(), 1)), state.lastResult());
    }

    @Transactional
    public Page challenge(UUID playerId, Challenge request) {
        var player = players.findByIdForUpdate(playerId).orElseThrow(() -> new NotFoundException("Jogador não encontrado"));
        State state = repository.state(playerId);
        Instant now = Instant.now();
        if (state.lastResult() != null && state.lastResult().requestId().equals(request.requestId())) return page(state, now);
        if (request.expectedAttempts() != state.attempts()) throw new ConflictException("A torre foi atualizada. Atualize a página antes de desafiar novamente.");
        if (state.availableAt() != null && now.isBefore(state.availableAt())) throw new ConflictException("Aguarde os 30 segundos entre combates.");
        if (state.highestFloor() >= Long.MAX_VALUE - 1 || state.attempts() == Long.MAX_VALUE)
            throw new ConflictException("Limite técnico de representação da torre atingido.");
        var team = teams.findByIdAndPlayerIdForUpdate(request.teamId(), playerId)
                .orElseThrow(() -> new NotFoundException("Time não encontrado"));
        var ids = team.getDigimonIds();
        if (ids.isEmpty() || ids.size() > 3 || new HashSet<>(ids).size() != ids.size()) throw new BadRequestException("Time inválido");
        var members = digimons.findAllByIdForUpdate(playerId, ids);
        var usable = Set.of(DigimonStatus.ACTIVE, DigimonStatus.HATCHED, DigimonStatus.STORED);
        if (members.size() != ids.size() || members.stream().anyMatch(d -> d.isBot() || !usable.contains(d.getStatus())))
            throw new ConflictException("Um ou mais membros do time estão indisponíveis.");
        if (missions.existsByPlayerIdAndAnyDigimonIdAndStatusIn(playerId, ids, List.of(MissionStatus.RUNNING, MissionStatus.COMPLETED)))
            throw new ConflictException("Um membro do time está em missão. Finalize e resgate a missão antes de entrar na torre.");
        var stats = ids.stream().map(id -> members.stream().filter(d -> d.getId().equals(id)).findFirst().orElseThrow())
                .map(d -> power.effectiveStats(d, player.getClanId()))
                .map(s -> Stats.of(s.hp(), s.attack(), s.defense())).toList();
        Floor floor = TowerRules.floor(Math.addExact(state.highestFloor(), 1));
        Combat combat = TowerRules.fight(stats, floor.stats());
        Reward reward = combat.won() ? floor.reward() : new Reward(0, null, null, 0);
        if (combat.won()) {
            if ((long) player.getBits() + reward.bits() > Integer.MAX_VALUE)
                throw new ConflictException("Saldo de Bits no limite. Libere saldo antes de receber a recompensa.");
            var item = items.findByCode(reward.itemCode()).orElseThrow(() -> new ConflictException("Recompensa da torre indisponível."));
            grant.addMaterialToPlayer(playerId, item, reward.quantity());
            player.addBits(reward.bits());
            players.save(player);
        }
        Result result = new Result(request.requestId(), floor.number(), combat.won(), combat.rounds(), team.getName(), floor.enemyName(), reward);
        State updated = new State(combat.won() ? floor.number() : state.highestFloor(), Math.addExact(state.attempts(), 1),
                now.plusSeconds(TowerRules.COOLDOWN_SECONDS), result);
        repository.save(playerId, updated);
        audit.success("tower:" + playerId + ":" + result.requestId(), "TOWER_CHALLENGED", "Player", playerId.toString(),
                Map.of("floor", result.floor(), "won", result.won(), "rounds", result.rounds(),
                        "teamId", request.teamId().toString(), "bits", reward.bits(), "quantity", reward.quantity()));
        return page(updated, now);
    }
}
