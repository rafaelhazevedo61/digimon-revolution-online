package com.dro.modules.loot.api;

import com.dro.modules.loot.api.dto.request.LootTableAdminRequest;
import com.dro.modules.loot.api.dto.response.AdminLootItemCatalogResponse;
import com.dro.modules.loot.api.dto.response.AdminLootTableResponse;
import com.dro.modules.loot.application.AdminLootTableUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * API administrativa para manutenção das Loot Tables nomeadas.
 */
@RestController
@RequestMapping("/admin/loot-tables")
public class AdminLootTableController {
    private final AdminLootTableUseCase adminLootTableUseCase;

    @PostMapping
    public ResponseEntity<AdminLootTableResponse> create(@RequestHeader("Authorization") String authorization, @RequestBody @Valid LootTableAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminLootTableUseCase.create(authorization, request));
    }

    @GetMapping
    public ResponseEntity<List<AdminLootTableResponse>> list(@RequestParam(required = false) Boolean activeOnly) {
        return ResponseEntity.ok(adminLootTableUseCase.list(activeOnly));
    }

    @GetMapping("/catalog/items")
    public ResponseEntity<List<AdminLootItemCatalogResponse>> catalog(@RequestParam(required = false) String category) {
        return ResponseEntity.ok(adminLootTableUseCase.catalog(category));
    }

    @GetMapping("/{code}")
    public ResponseEntity<AdminLootTableResponse> get(@PathVariable String code) {
        return ResponseEntity.ok(adminLootTableUseCase.get(code));
    }

    @PutMapping("/{code}")
    public ResponseEntity<AdminLootTableResponse> update(@RequestHeader("Authorization") String authorization, @PathVariable String code, @RequestBody @Valid LootTableAdminRequest request) {
        return ResponseEntity.ok(adminLootTableUseCase.update(authorization, code, request));
    }

    @PatchMapping("/{code}/toggle-active")
    public ResponseEntity<AdminLootTableResponse> toggleActive(@RequestHeader("Authorization") String authorization, @PathVariable String code) {
        return ResponseEntity.ok(adminLootTableUseCase.toggleActive(authorization, code));
    }

    public AdminLootTableController(final AdminLootTableUseCase adminLootTableUseCase) {
        this.adminLootTableUseCase = adminLootTableUseCase;
    }
}
