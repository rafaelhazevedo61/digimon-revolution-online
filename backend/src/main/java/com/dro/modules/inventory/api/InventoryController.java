package com.dro.modules.inventory.api;

import com.dro.modules.inventory.api.dto.request.GrantItemRequest;
import com.dro.modules.inventory.api.dto.request.UseItemRequest;
import com.dro.modules.inventory.api.dto.response.GrantItemResponse;
import com.dro.modules.inventory.application.AddItemUseCase;
import com.dro.modules.inventory.application.UseItemUseCase;
import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
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
    private final PlayerRepository playerRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;

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
        ItemDefinition itemDef = itemDefinitionRepository.findByCode(request.itemCode())
                .orElseThrow(() -> new NotFoundException("Item not found: " + request.itemCode()));

        addItemUseCase.addMaterial(request.digimonId(), itemDef, request.quantity());

        return ResponseEntity.ok(new GrantItemResponse(
                request.digimonId(),
                itemDef.getCode(),
                request.quantity(),
                "Item granted successfully"
        ));
    }
}