package com.dro.modules.loot.api;

import com.dro.modules.loot.api.dto.response.ChestLootPreviewResponse;
import com.dro.modules.loot.application.GetChestLootPreviewUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/loot/chests")
public class ChestLootPreviewController {
    private final GetChestLootPreviewUseCase getChestLootPreviewUseCase;

    @GetMapping("/{code}/preview")
    public ChestLootPreviewResponse preview(@RequestHeader("Authorization") String authorization,
                                             @PathVariable String code) {
        return getChestLootPreviewUseCase.execute(code);
    }

    public ChestLootPreviewController(GetChestLootPreviewUseCase getChestLootPreviewUseCase) {
        this.getChestLootPreviewUseCase = getChestLootPreviewUseCase;
    }
}
