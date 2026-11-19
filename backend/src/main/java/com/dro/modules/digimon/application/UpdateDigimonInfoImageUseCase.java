package com.dro.modules.digimon.application;

import com.dro.modules.digimon.api.dto.request.UpdateDigimonInfoImageRequest;
import com.dro.modules.digimon.api.dto.response.DigimonInfoResponse;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateDigimonInfoImageUseCase {
    private final DigimonInfosRepository digimonInfosRepository;

    @Transactional
    public DigimonInfoResponse execute(Long id, UpdateDigimonInfoImageRequest request) {
        DigimonInfos info = digimonInfosRepository.findById(id).orElseThrow(() -> new NotFoundException("Digimon Info não encontrado: " + id));
        String imageUrl = request.imageUrl();
        info.setImageUrl(imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim());
        return DigimonInfoResponse.from(digimonInfosRepository.save(info));
    }

    public UpdateDigimonInfoImageUseCase(final DigimonInfosRepository digimonInfosRepository) {
        this.digimonInfosRepository = digimonInfosRepository;
    }
}
