package com.dro.modules.digitama.application;

import com.dro.modules.digitama.config.DigitamaConfig;
import com.dro.modules.digitama.domain.DigitamaPool;
import com.dro.modules.digitama.domain.DigitamaPoolEntry;
import com.dro.modules.digitama.domain.enums.DigitamaType;
import com.dro.modules.digimon.domain.enums.Stage;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Aplica as regras temporárias de elegibilidade dos pools de Digitama.
 */
@Service
public class DigitamaPoolEligibilityService {
    private final DigitamaConfig digitamaConfig;

    public List<DigitamaPoolEntry> getEligibleEntries(DigitamaPool pool) {
        DigitamaType digitamaType = DigitamaType.fromPoolCode(pool.getCode());

        return pool.getEntries().stream()
                .filter(DigitamaPoolEntry::isActive)
                .filter(entry -> entry.getWeight() > 0)
                .filter(entry -> entry.getDigimonInfo() != null)
                .filter(entry -> entry.getDigimonInfo().getStage() == Stage.BABY)
                .filter(entry -> digitamaType.accepts(entry.getDigimonInfo().getElement()))
                .filter(entry -> digitamaConfig.isBabyEnabled(entry.getDigimonInfo().getName()))
                .toList();
    }

    public DigitamaPoolEligibilityService(final DigitamaConfig digitamaConfig) {
        this.digitamaConfig = digitamaConfig;
    }
}
