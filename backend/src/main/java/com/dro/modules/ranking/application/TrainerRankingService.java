package com.dro.modules.ranking.application;

import com.dro.modules.ranking.infra.TrainerRankingRepository;
import com.dro.modules.ranking.api.dto.response.TrainerRankingDtos.*;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class TrainerRankingService {
    private final TrainerRankingRepository repository;
    public TrainerRankingService(TrainerRankingRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Page page(UUID playerId, int page, int size) {
        if (page < 0 || size < 1 || size > 50) throw new BadRequestException("Página inválida. Use página >= 0 e tamanho entre 1 e 50.");
        var eligibility = repository.eligibility(playerId);
        if (eligibility.isEmpty()) throw new NotFoundException("Jogador não encontrado.");
        var state = repository.state();
        var me = eligibility.get(0) ? repository.me(playerId) : null;
        return new Page(repository.page(page, size), me,
                !eligibility.get(0) ? "EXCLUDED" : me == null ? "PENDING" : "RANKED",
                state.updatedAt(), state.totalPlayers(), page, size, ((long) page + 1) * size < state.totalPlayers());
    }
}
