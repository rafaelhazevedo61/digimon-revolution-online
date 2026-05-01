package com.dro.modules.inventory.api;

import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.application.UseItemUseCase;
import com.dro.modules.inventory.infra.InventoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryRepository repository;
    private final UseItemUseCase useItemUseCase;
    private final AddItemUseCase addItemUseCase;

    @GetMapping
    public ResponseEntity<?> getInventory(
            @RequestHeader("Authorization") String authorization
    ) {
        UUID playerId = UUID.fromString(authorization.split(":")[1]);
        return ResponseEntity.ok(repository.findByPlayerId(playerId));
    }

    @PostMapping("/use")
    public ResponseEntity<Void> useItem(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid UseItemRequest request
    ) {
        useItemUseCase.execute(authorization, request.itemType());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/grant")
    public ResponseEntity<GrantItemResponse> grantItem(
            @RequestBody @Valid GrantItemRequest request
    ) {
        addItemUseCase.execute(request.playerId(), request.itemType(), request.quantity());
        return ResponseEntity.ok(new GrantItemResponse(
                request.playerId(),
                request.itemType(),
                request.quantity(),
                "Item granted successfully"
        ));
    }
}
