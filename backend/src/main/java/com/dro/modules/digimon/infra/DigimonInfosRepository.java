package com.dro.modules.digimon.infra;

import com.dro.modules.digimon.domain.DigimonInfos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DigimonInfosRepository extends JpaRepository<DigimonInfos, Long> {

    Optional<DigimonInfos> findByName (String name);
}
