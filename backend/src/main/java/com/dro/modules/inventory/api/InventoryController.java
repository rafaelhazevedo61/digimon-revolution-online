package com.dro.modules.inventory.api;

import com.dro.modules.inventory.api.dto.request.UseItemRequest;
import com.dro.modules.inventory.application.UseItemUseCase;
import com.dro.modules.loot.api.dto.request.OpenChestRequest;
import com.dro.modules.loot.api.dto.response.ChestOpeningResponse;
import com.dro.modules.loot.application.OpenChestUseCase;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Componente da camada de controller da API do módulo de Inventário.
 */
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryRepository repository;
    private final UseItemUseCase useItemUseCase;
    private final OpenChestUseCase openChestUseCase;
    private final PlayerRepository playerRepository;

    @GetMapping
    public ResponseEntity<?> getInventory(
            @RequestHeader("Authorization") String authorization
    ) {
        UUID playerId = TokenExtractor.extractPlayerId(authorization);

        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }

        return ResponseEntity.ok(repository.findByDigimonId(player.getActiveDigimonId()));
    }

    @PostMapping("/chests/open")
    public ResponseEntity<ChestOpeningResponse> openChest(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid OpenChestRequest request
    ) {
        return ResponseEntity.ok(openChestUseCase.execute(authorization, request));
    }

    @PostMapping("/use")
    public ResponseEntity<Void> useItem(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid UseItemRequest request
    ) {
        useItemUseCase.execute(authorization, request.itemType());
        return ResponseEntity.ok().build();
    }
}