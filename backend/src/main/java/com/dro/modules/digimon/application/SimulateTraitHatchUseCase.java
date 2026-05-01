package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.response.TraitHatchSimulationResponse;
import com.dro.modules.digimon.domain.TraitRoller;
import com.dro.modules.digimon.domain.enums.Trait;
import com.dro.shared.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SimulateTraitHatchUseCase {

    public TraitHatchSimulationResponse execute(int attempts) {

        if (attempts <= 0) {
            throw new BadRequestException("Attempts must be greater than zero");
        }

        Map<String, Integer> distribution = new HashMap<>();

        int withTrait = 0;

        for (int i = 0; i < attempts; i++) {

            Trait trait = TraitRoller.rollForNormalHatch();

            if (trait != null) {
                withTrait++;
                distribution.merge(trait.name(), 1, Integer::sum);
            }
        }

        int withoutTrait = attempts - withTrait;

        double traitRate = (withTrait * 100.0) / attempts;

        return new TraitHatchSimulationResponse(
                attempts,
                withTrait,
                withoutTrait,
                traitRate,
                distribution
        );
    }
}