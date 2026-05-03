package com.dro.modules.digitama.infra;

import com.dro.modules.digitama.domain.DigitamaPool;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DigitamaPoolRepository extends JpaRepository<DigitamaPool, Long> {

    @EntityGraph(attributePaths = {
            "content",
            "entries",
            "entries.digimonInfo"
    })
    List<DigitamaPool> findByActiveTrueAndContentActiveTrue();
}