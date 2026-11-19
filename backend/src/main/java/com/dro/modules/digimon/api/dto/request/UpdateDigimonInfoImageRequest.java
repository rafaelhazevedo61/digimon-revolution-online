package com.dro.modules.digimon.api.dto.request;

import jakarta.validation.constraints.Size;

/** Atualização administrativa da imagem de referência de um Digimon do catálogo. */
public record UpdateDigimonInfoImageRequest(
        @Size(max = 1000) String imageUrl
) {
}
