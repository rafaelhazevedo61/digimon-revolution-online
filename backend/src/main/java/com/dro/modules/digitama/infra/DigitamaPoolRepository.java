package com.dro.modules.digitama.infra;

import com.dro.modules.digitama.domain.DigitamaPool;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DigitamaPoolRepository extends JpaRepository<DigitamaPool, Long> {

    @EntityGraph(attributePaths = {
            "content",
            "entries",
            "entries.digimonInfo"
    })
    List<DigitamaPool> findByActiveTrueAndContentActiveTrue();


    @EntityGraph(attributePaths = {
            "content",
            "entries",
            "entries.digimonInfo"
    })
    Optional<DigitamaPool> findByCodeAndActiveTrueAndContentActiveTrue(String code);

}