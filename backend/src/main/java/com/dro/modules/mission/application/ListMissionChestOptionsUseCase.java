package com.dro.modules.mission.application;

import com.dro.modules.loot.infra.ChestDefinitionRepository;
import com.dro.modules.mission.api.dto.response.MissionChestOptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lista os Baús da Área ativos que podem ser associados a uma Missão.
 */
@Service
@RequiredArgsConstructor
public class ListMissionChestOptionsUseCase {

    private final ChestDefinitionRepository chestDefinitionRepository;

    /** Retorna baús ativos com a Loot Table nomeada vinculada. */
    @Transactional(readOnly = true)
    public List<MissionChestOptionResponse> execute() {
        return chestDefinitionRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(MissionChestOptionResponse::from)
                .toList();
    }
}
