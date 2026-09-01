package com.dro.modules.mission.application;

import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.mission.api.dto.request.SaveMissionTeamRequest;
import com.dro.modules.mission.api.dto.response.MissionTeamResponse;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.domain.MissionTeam;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.mission.infra.MissionTeamRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class MissionTeamUseCase {
    private static final Set<DigimonStatus> USABLE_STATUSES = EnumSet.of(
            DigimonStatus.ACTIVE,
            DigimonStatus.HATCHED,
            DigimonStatus.STORED
    );

    private final MissionTeamRepository missionTeamRepository;
    private final MissionInstanceRepository missionInstanceRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;

    public List<MissionTeamResponse> list(String token) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        return missionTeamRepository.findByPlayerIdOrderByCreatedAtAsc(playerId)
                .stream()
                .map(MissionTeamResponse::from)
                .toList();
    }

    @Transactional
    public MissionTeamResponse create(String token, SaveMissionTeamRequest request) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        lockPlayer(playerId);
        List<UUID> digimonIds = validateRequest(playerId, null, request);
        MissionTeam team = new MissionTeam(playerId, normalizeName(request.name()), digimonIds, request.captainDigimonId());
        return MissionTeamResponse.from(missionTeamRepository.save(team));
    }

    @Transactional
    public MissionTeamResponse update(String token, UUID teamId, SaveMissionTeamRequest request) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        lockPlayer(playerId);
        MissionTeam team = missionTeamRepository.findByIdAndPlayerIdForUpdate(teamId, playerId)
                .orElseThrow(() -> new NotFoundException("Time não encontrado"));
        if (missionInstanceRepository.existsByTeamIdAndStatusIn(
                teamId,
                List.of(MissionStatus.RUNNING, MissionStatus.COMPLETED)
        )) {
            throw new ConflictException("O time está vinculado a uma missão em andamento");
        }
        List<UUID> digimonIds = validateRequest(playerId, teamId, request);
        team.update(normalizeName(request.name()), digimonIds, request.captainDigimonId());
        return MissionTeamResponse.from(missionTeamRepository.save(team));
    }

    @Transactional
    public void delete(String token, UUID teamId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        lockPlayer(playerId);
        MissionTeam team = missionTeamRepository.findByIdAndPlayerIdForUpdate(teamId, playerId)
                .orElseThrow(() -> new NotFoundException("Time não encontrado"));
        if (missionInstanceRepository.existsByTeamIdAndStatusIn(
                teamId,
                List.of(MissionStatus.RUNNING, MissionStatus.COMPLETED)
        )) {
            throw new ConflictException("O time está vinculado a uma missão em andamento");
        }
        missionTeamRepository.delete(team);
    }

    public MissionTeam getOwnedTeam(UUID playerId, UUID teamId) {
        return missionTeamRepository.findByIdAndPlayerId(teamId, playerId)
                .orElseThrow(() -> new NotFoundException("Time não encontrado"));
    }

    private List<UUID> validateRequest(UUID playerId, UUID teamId, SaveMissionTeamRequest request) {
        if (request == null || request.digimonIds() == null || request.digimonIds().isEmpty() || request.digimonIds().size() > 3) {
            throw new BadRequestException("Um time precisa ter entre 1 e 3 Digimons");
        }
        List<UUID> digimonIds = request.digimonIds();
        if (digimonIds.stream().anyMatch(id -> id == null) || new HashSet<>(digimonIds).size() != digimonIds.size()) {
            throw new BadRequestException("Um time não pode repetir Digimons");
        }
        if (!new HashSet<>(digimonIds).contains(request.captainDigimonId())) {
            throw new BadRequestException("O capitão precisa pertencer ao time");
        }

        var digimons = digimonRepository.findAllByIdForUpdate(playerId, digimonIds);
        if (digimons.size() != digimonIds.size()) {
            throw new NotFoundException("Um ou mais Digimons não pertencem ao jogador");
        }
        if (digimons.stream().anyMatch(digimon -> !USABLE_STATUSES.contains(digimon.getStatus()))) {
            throw new ConflictException("Um ou mais Digimons não estão disponíveis para formar um time");
        }

        List<MissionTeam> conflictingTeams = teamId == null
                ? missionTeamRepository.findByPlayerIdAndDigimonIds(playerId, digimonIds)
                : missionTeamRepository.findOtherByPlayerIdAndDigimonIds(playerId, teamId, digimonIds);
        if (!conflictingTeams.isEmpty()) {
            String teamName = conflictingTeams.get(0).getName();
            throw new ConflictException("Um ou mais Digimons já pertencem ao time " + teamName);
        }
        return digimonIds;
    }

    private void lockPlayer(UUID playerId) {
        if (playerRepository != null) {
            playerRepository.findByIdForUpdate(playerId)
                    .orElseThrow(() -> new NotFoundException("Jogador não encontrado"));
        }
    }

    private String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isBlank()) {
            throw new BadRequestException("O nome do time é obrigatório");
        }
        return normalized;
    }

    public MissionTeamUseCase(
            MissionTeamRepository missionTeamRepository,
            MissionInstanceRepository missionInstanceRepository,
            DigimonRepository digimonRepository
    ) {
        this(null, missionTeamRepository, missionInstanceRepository, digimonRepository);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public MissionTeamUseCase(
            PlayerRepository playerRepository,
            MissionTeamRepository missionTeamRepository,
            MissionInstanceRepository missionInstanceRepository,
            DigimonRepository digimonRepository
    ) {
        this.playerRepository = playerRepository;
        this.missionTeamRepository = missionTeamRepository;
        this.missionInstanceRepository = missionInstanceRepository;
        this.digimonRepository = digimonRepository;
    }
}
