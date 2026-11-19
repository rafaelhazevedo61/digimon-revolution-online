package com.dro.modules.loot.api;

import com.dro.modules.loot.api.dto.request.AdminChestUpdateRequest;
import com.dro.modules.loot.api.dto.response.AdminChestResponse;
import com.dro.modules.loot.application.AdminChestUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * API administrativa para manutenção dos Baús temáticos do jogo.
 */
@RestController
@RequestMapping("/admin/chests")
public class AdminChestController {
    private final AdminChestUseCase adminChestUseCase;

    @GetMapping
    public ResponseEntity<List<AdminChestResponse>> list(@RequestParam(required = false) Boolean activeOnly) {
        return ResponseEntity.ok(adminChestUseCase.list(activeOnly));
    }

    @GetMapping("/{code}")
    public ResponseEntity<AdminChestResponse> get(@PathVariable String code) {
        return ResponseEntity.ok(adminChestUseCase.get(code));
    }

    @PutMapping("/{code}")
    public ResponseEntity<AdminChestResponse> update(@RequestHeader("Authorization") String authorization, @PathVariable String code, @RequestBody @Valid AdminChestUpdateRequest request) {
        return ResponseEntity.ok(adminChestUseCase.update(authorization, code, request));
    }

    @PatchMapping("/{code}/toggle-active")
    public ResponseEntity<AdminChestResponse> toggleActive(@RequestHeader("Authorization") String authorization, @PathVariable String code) {
        return ResponseEntity.ok(adminChestUseCase.toggleActive(authorization, code));
    }

    public AdminChestController(final AdminChestUseCase adminChestUseCase) {
        this.adminChestUseCase = adminChestUseCase;
    }
}
