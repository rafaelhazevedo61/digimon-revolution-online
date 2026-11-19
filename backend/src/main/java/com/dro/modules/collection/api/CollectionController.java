package com.dro.modules.collection.api;

import com.dro.modules.collection.api.dto.CollectionDtos;
import com.dro.modules.collection.application.CollectionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collection")
public class CollectionController {
    private final CollectionUseCase collectionUseCase;

    @GetMapping
    public ResponseEntity<CollectionDtos.SummaryResponse> summary(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok(collectionUseCase.summary(authorization));
    }

    @PostMapping("/register")
    public ResponseEntity<CollectionDtos.RegisterResponse> register(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid CollectionDtos.RegisterRequest request) {
        return ResponseEntity.ok(collectionUseCase.register(authorization, request.digimonId()));
    }

    public CollectionController(CollectionUseCase collectionUseCase) {
        this.collectionUseCase = collectionUseCase;
    }
}
