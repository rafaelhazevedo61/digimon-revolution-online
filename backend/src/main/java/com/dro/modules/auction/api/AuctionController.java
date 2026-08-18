package com.dro.modules.auction.api;

import com.dro.modules.auction.api.dto.request.BuyAuctionListingRequest;
import com.dro.modules.auction.api.dto.request.CreateAuctionListingRequest;
import com.dro.modules.auction.api.dto.response.AuctionListingPageResponse;
import com.dro.modules.auction.api.dto.response.AuctionListingResponse;
import com.dro.modules.auction.api.dto.response.AuctionPurchaseResponse;
import com.dro.modules.auction.api.dto.response.AuctionTransactionResponse;
import com.dro.modules.auction.application.BuyAuctionListingUseCase;
import com.dro.modules.auction.application.CancelAuctionListingUseCase;
import com.dro.modules.auction.application.CreateAuctionListingUseCase;
import com.dro.modules.auction.application.GetAuctionHistoryUseCase;
import com.dro.modules.auction.application.ListAuctionListingsUseCase;
import com.dro.modules.auction.application.ListMyAuctionListingsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auction")
@RequiredArgsConstructor
public class AuctionController {

    private final ListAuctionListingsUseCase listAuctionListingsUseCase;
    private final ListMyAuctionListingsUseCase listMyAuctionListingsUseCase;
    private final GetAuctionHistoryUseCase getAuctionHistoryUseCase;
    private final CreateAuctionListingUseCase createAuctionListingUseCase;
    private final BuyAuctionListingUseCase buyAuctionListingUseCase;
    private final CancelAuctionListingUseCase cancelAuctionListingUseCase;

    public AuctionController (ListAuctionListingsUseCase listAuctionListingsUseCase, ListMyAuctionListingsUseCase listMyAuctionListingsUseCase, GetAuctionHistoryUseCase getAuctionHistoryUseCase, CreateAuctionListingUseCase createAuctionListingUseCase, BuyAuctionListingUseCase buyAuctionListingUseCase, CancelAuctionListingUseCase cancelAuctionListingUseCase) {
        this.listAuctionListingsUseCase = listAuctionListingsUseCase;
        this.listMyAuctionListingsUseCase = listMyAuctionListingsUseCase;
        this.getAuctionHistoryUseCase = getAuctionHistoryUseCase;
        this.createAuctionListingUseCase = createAuctionListingUseCase;
        this.buyAuctionListingUseCase = buyAuctionListingUseCase;
        this.cancelAuctionListingUseCase = cancelAuctionListingUseCase;
    }

    @GetMapping("/listings")
    public ResponseEntity<AuctionListingPageResponse> list(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String rarity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(listAuctionListingsUseCase.execute(
                authorization, search, category, rarity, page, size));
    }

    @GetMapping("/my-listings")
    public ResponseEntity<AuctionListingPageResponse> myListings(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(listMyAuctionListingsUseCase.execute(authorization, page, size));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AuctionTransactionResponse>> history(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(getAuctionHistoryUseCase.execute(authorization, page, size));
    }

    @PostMapping("/listings")
    public ResponseEntity<AuctionListingResponse> create(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid CreateAuctionListingRequest request
    ) {
        return ResponseEntity.ok(createAuctionListingUseCase.execute(authorization, request));
    }

    @PostMapping("/listings/{listingId}/buy")
    public ResponseEntity<AuctionPurchaseResponse> buy(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID listingId,
            @RequestBody @Valid BuyAuctionListingRequest request
    ) {
        return ResponseEntity.ok(buyAuctionListingUseCase.execute(authorization, listingId, request));
    }

    @PostMapping("/listings/{listingId}/cancel")
    public ResponseEntity<AuctionListingResponse> cancel(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID listingId
    ) {
        return ResponseEntity.ok(cancelAuctionListingUseCase.execute(authorization, listingId));
    }
}
