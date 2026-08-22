package com.dro.modules.boss.world.application;

import com.dro.modules.boss.world.domain.WorldBossInstance;
import com.dro.modules.boss.world.domain.WorldBossStatus;
import com.dro.modules.boss.world.infra.WorldBossInstanceRepository;
import com.dro.shared.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Abre manualmente um novo ciclo do Boss Mundial sem modificar o histórico dos ciclos anteriores.
 */
@Service
@RequiredArgsConstructor
public class AdminForceNewWorldBossCycleUseCase {

    private final WorldBossInstanceRepository worldBossInstanceRepository;
    private final WorldBossInstanceFactory worldBossInstanceFactory;

    @Transactional
    public WorldBossInstance execute() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        WorldBossInstance current = worldBossInstanceRepository
                .findFirstByBossDateOrderByCreatedAtDesc(today)
                .orElseThrow(() -> new ConflictException(
                        "Não existe uma instância do Boss Mundial hoje para substituir."));

        if (current.getStatus() != WorldBossStatus.DEFEATED) {
            throw new ConflictException(
                    "O novo ciclo só pode ser aberto depois que o Boss Mundial atual for derrotado.");
        }

        try {
            return worldBossInstanceFactory.create(today, current.getCycleNumber() + 1);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException(
                    "Já existe um novo ciclo do Boss Mundial para hoje.");
        }
    }
}
