package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.response.DigimonInfoPageResponse;
import com.dro.modules.digimon.api.dto.response.DigimonInfoResponse;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.digimon.infra.spec.DigimonInfosSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetDigimonInfosUseCase {

    private final DigimonInfosRepository digimonInfosRepository;

    public DigimonInfoPageResponse execute(
            String name,
            String stage,
            String attribute,
            String element,
            String specie,
            Pageable pageable
    ) {
        Page<DigimonInfoResponse> digimonInfos = digimonInfosRepository.findAll(
                        DigimonInfosSpecifications.withFilters(
                                name,
                                stage,
                                attribute,
                                element,
                                specie
                        ),
                        pageable
                )
                .map(DigimonInfoResponse::from);

        return DigimonInfoPageResponse.from(digimonInfos);
    }
}