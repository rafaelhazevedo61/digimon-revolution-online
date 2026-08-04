package com.dro.modules.arena.application;

import com.dro.modules.arena.api.dto.response.ArenaHistoryEntryResponse;
import com.dro.modules.arena.domain.ArenaMatch;
import com.dro.modules.arena.infra.ArenaMatchRepository;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetArenaHistoryUseCase {

    private static final int MAX_SIZE = 50;

    private final ArenaMatchRepository arenaMatchRepository;
    private final DigimonRepository digimonRepository;

    public List<ArenaHistoryEntryResponse> execute(String token, int page, int size) {
        UUID playerId = TokenExtractor.extractPlayerId(token);

        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 20 : Math.min(size, MAX_SIZE);

        Page<ArenaMatch> matches = arenaMatchRepository
                .findByAttackerPlayerIdOrDefenderPlayerIdOrderByCreatedAtDesc(
                        playerId, playerId, PageRequest.of(safePage, safeSize));

        List<ArenaMatch> content = matches.getContent();

        List<UUID> digimonIds = content.stream()
                .flatMap(m -> java.util.stream.Stream.of(m.getAttackerDigimonId(), m.getDefenderDigimonId()))
                .distinct()
                .toList();

        Map<UUID, String> names = digimonRepository.findAllById(digimonIds)
                .stream()
                .collect(Collectors.toMap(Digimon::getId, Digimon::getName));

        return content.stream().map(m -> {
            boolean isAttacker = m.getAttackerPlayerId().equals(playerId);
            boolean won = isAttacker ? m.isAttackerWon() : !m.isAttackerWon();
            UUID opponentDigimonId = isAttacker ? m.getDefenderDigimonId() : m.getAttackerDigimonId();
            String opponentName = names.getOrDefault(opponentDigimonId, "Unknown");
            int ratingChange = isAttacker ? m.getAttackerRatingChange() : m.getDefenderRatingChange();
            int myPower = isAttacker ? m.getAttackerPower() : m.getDefenderPower();
            int oppPower = isAttacker ? m.getDefenderPower() : m.getAttackerPower();
            int bits = isAttacker ? m.getBitsGained() : 0;

            return new ArenaHistoryEntryResponse(
                    isAttacker,
                    won,
                    opponentName,
                    myPower,
                    oppPower,
                    ratingChange,
                    bits,
                    m.getCreatedAt()
            );
        }).toList();
    }
}
