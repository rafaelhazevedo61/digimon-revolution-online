package com.dro.modules.boss.api;

import com.dro.modules.equipment.api.dto.request.UpdateEquipmentRarityProfileRequest;
import com.dro.modules.equipment.api.dto.response.AdminEquipmentRarityProfileResponse;
import com.dro.modules.equipment.application.EquipmentRarityProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Administração dos perfis de raridade de equipamentos usados por Bosses.
 */
@RestController
@RequestMapping("/admin/bosses/rarity-profiles")
@RequiredArgsConstructor
public class AdminBossRarityProfileController {

    private final EquipmentRarityProfileService profileService;

    /** Lista os percentuais atuais de raridade por tipo de Boss. */
    @GetMapping
    public ResponseEntity<List<AdminEquipmentRarityProfileResponse>> list() {
        return ResponseEntity.ok(profileService.listBossProfiles());
    }

    /** Atualiza os percentuais de um tipo de Boss. */
    @PutMapping("/{profileKey}")
    public ResponseEntity<AdminEquipmentRarityProfileResponse> update(
            @PathVariable String profileKey,
            @RequestBody @Valid UpdateEquipmentRarityProfileRequest request
    ) {
        return ResponseEntity.ok(profileService.updateBossProfile(profileKey, request));
    }
}
