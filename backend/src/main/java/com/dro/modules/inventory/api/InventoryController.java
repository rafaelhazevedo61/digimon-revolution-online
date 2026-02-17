package com.dro.modules.inventory.api;

import com.dro.modules.inventory.infra.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryRepository repository;

    @GetMapping
    public ResponseEntity<?> getInventory(
            @RequestHeader("Authorization") String authorization
    ) {
        UUID playerId = UUID.fromString(authorization.split(":")[1]);
        return ResponseEntity.ok(repository.findByPlayerId(playerId));
    }
}
