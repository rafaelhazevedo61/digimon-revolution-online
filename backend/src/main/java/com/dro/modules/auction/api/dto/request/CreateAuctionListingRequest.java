package com.dro.modules.auction.api.dto.request;

import com.dro.modules.auction.domain.AuctionListingType;
import com.dro.shared.exception.BadRequestException;
import jakarta.validation.constraints.Min;
import java.util.UUID;

/** Exactly one asset identifier is required. Legacy item payloads remain supported. */
public record CreateAuctionListingRequest(
        Long itemDefinitionId,
        @Min(1) int quantity,
        @Min(1) int unitPrice,
        @Min(1) int durationHours,
        UUID equipmentId,
        AuctionListingType listingType
) {
    public CreateAuctionListingRequest(Long itemDefinitionId, int quantity, int unitPrice, int durationHours) {
        this(itemDefinitionId, quantity, unitPrice, durationHours, null, null);
    }

    public AuctionListingType resolvedType() {
        if ((itemDefinitionId == null) == (equipmentId == null)) {
            throw new BadRequestException("Informe exatamente um item ou equipamento");
        }
        AuctionListingType inferred = equipmentId == null ? AuctionListingType.ITEM : AuctionListingType.EQUIPMENT;
        if (listingType != null && listingType != inferred) {
            throw new BadRequestException("Tipo de anúncio incompatível com o identificador");
        }
        if (inferred == AuctionListingType.EQUIPMENT && quantity != 1) {
            throw new BadRequestException("Equipamentos devem ser publicados individualmente");
        }
        return inferred;
    }
}
