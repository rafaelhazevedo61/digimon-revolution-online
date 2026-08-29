package com.dro.modules.clan.storage.api;

import com.dro.modules.clan.storage.api.dto.request.ClanStorageItemRequest;
import com.dro.modules.clan.storage.api.dto.response.ClanStorageResponse;
import com.dro.modules.clan.storage.application.ClanStorageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/clans/{clanId}/storage")
public class ClanStorageController {
    private final ClanStorageService storageService;

    @GetMapping
    public ResponseEntity<ClanStorageResponse> get(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID clanId
    ) {
        return ResponseEntity.ok(storageService.get(authorization, clanId));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ClanStorageResponse> deposit(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID clanId,
            @RequestBody @Valid ClanStorageItemRequest request
    ) {
        return ResponseEntity.ok(storageService.deposit(authorization, clanId, request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ClanStorageResponse> withdraw(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID clanId,
            @RequestBody @Valid ClanStorageItemRequest request
    ) {
        return ResponseEntity.ok(storageService.withdraw(authorization, clanId, request));
    }

    public ClanStorageController(final ClanStorageService storageService) {
        this.storageService = storageService;
    }
}
