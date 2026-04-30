package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.response.DigimonLineageItemResponse;
import com.dro.modules.digimon.api.dto.response.DigimonLineageResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetDigimonLineageUseCase {

    private final DigimonRepository digimonRepository;

    public DigimonLineageResponse execute(String token, UUID digimonId) {

        UUID playerId = extractPlayerId(token);

        Digimon currentDigimon = digimonRepository.findById(digimonId)
                .orElseThrow(() -> new RuntimeException("Digimon not found"));

        validateOwner(currentDigimon, playerId);

        var lineage = new ArrayList<DigimonLineageItemResponse>();

        Digimon cursor = currentDigimon;

        while (cursor != null) {

            validateOwner(cursor, playerId);

            lineage.add(toLineageItem(cursor));

            if (cursor.getRebornedFrom() == null) {
                break;
            }

            cursor = digimonRepository.findById(cursor.getRebornedFrom())
                    .orElse(null);
        }

        Collections.reverse(lineage);

        return new DigimonLineageResponse(
                currentDigimon.getId(),
                lineage.size(),
                lineage
        );
    }

    private UUID extractPlayerId(String token) {
        return UUID.fromString(token.split(":")[1]);
    }

    private void validateOwner(Digimon digimon, UUID playerId) {
        if (!digimon.getPlayerId().equals(playerId)) {
            throw new RuntimeException("This Digimon does not belong to the player");
        }
    }

    private DigimonLineageItemResponse toLineageItem(Digimon digimon) {
        return new DigimonLineageItemResponse(
                digimon.getId(),
                digimon.getName(),
                digimon.getType(),
                digimon.getStage(),
                digimon.getLevel(),
                digimon.getIvHp(),
                digimon.getIvAttack(),
                digimon.getIvDefense(),
                digimon.getRarity(),
                digimon.getPersonality(),
                digimon.getTrait(),
                digimon.getRebirthCount(),
                digimon.getStatus(),
                digimon.getRebornedFrom(),
                digimon.getCreatedAt()
        );
    }
}