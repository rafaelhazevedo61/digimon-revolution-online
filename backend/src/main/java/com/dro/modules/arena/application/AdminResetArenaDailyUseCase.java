package com.dro.modules.arena.application;

import com.dro.modules.arena.infra.ArenaMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AdminResetArenaDailyUseCase {

    private final ArenaMatchRepository arenaMatchRepository;

    @Transactional
    public long execute() {
        Instant startOfDay = LocalDate.now(ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        return arenaMatchRepository.deleteByCreatedAtGreaterThanEqual(startOfDay);
    }
}
