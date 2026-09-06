package com.dro.modules.auction.domain;

import com.dro.modules.auction.api.dto.request.CreateAuctionListingRequest;
import com.dro.shared.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AuctionAssetRequestTest {
    @Test
    void infersLegacyItemsAndEquipmentAndRejectsAmbiguity() {
        UUID equipmentId = UUID.randomUUID();
        assertEquals(AuctionListingType.ITEM, new CreateAuctionListingRequest(1L, 3, 100, 24).resolvedType());
        assertEquals(AuctionListingType.EQUIPMENT,
                new CreateAuctionListingRequest(null, 1, 100, 24, equipmentId, null).resolvedType());
        assertThrows(BadRequestException.class,
                () -> new CreateAuctionListingRequest(null, 1, 100, 24).resolvedType());
        assertThrows(BadRequestException.class,
                () -> new CreateAuctionListingRequest(1L, 1, 100, 24, equipmentId, null).resolvedType());
        assertThrows(BadRequestException.class,
                () -> new CreateAuctionListingRequest(null, 2, 100, 24, equipmentId, null).resolvedType());
        assertThrows(BadRequestException.class,
                () -> new CreateAuctionListingRequest(null, 0, 100, 24, equipmentId, null).resolvedType());
        assertThrows(BadRequestException.class,
                () -> new CreateAuctionListingRequest(null, 1, 100, 24, equipmentId, AuctionListingType.ITEM).resolvedType());
        assertThrows(BadRequestException.class,
                () -> new CreateAuctionListingRequest(1L, 1, 100, 24, null, AuctionListingType.EQUIPMENT).resolvedType());
    }
}
