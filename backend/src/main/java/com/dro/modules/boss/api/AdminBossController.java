package com.dro.modules.boss.api;

import com.dro.modules.boss.api.dto.request.CreateBossDropRequest;
import com.dro.modules.boss.api.dto.request.CreateBossRequest;
import com.dro.modules.boss.api.dto.request.UpdateBossRequest;
import com.dro.modules.boss.domain.BossDefinitionEntity;
import com.dro.modules.boss.domain.BossDropEntity;
import com.dro.modules.boss.domain.BossType;
import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.boss.infra.BossDropRepository;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/bosses")
@RequiredArgsConstructor
public class AdminBossController {

    private final BossDefinitionRepository bossDefinitionRepository;
    private final BossDropRepository bossDropRepository;

    @GetMapping
    public ResponseEntity<List<BossDefinitionEntity>> listAll() {
        return ResponseEntity.ok(bossDefinitionRepository.findAllByOrderByIdAsc());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BossDefinitionEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                bossDefinitionRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Boss not found"))
        );
    }

    @PostMapping
    public ResponseEntity<BossDefinitionEntity> create(@RequestBody @Valid CreateBossRequest request) {
        BossDefinitionEntity boss = BossDefinitionEntity.builder()
                .code(request.code())
                .name(request.name())
                .bossType(BossType.valueOf(request.bossType()))
                .requiredStage(Stage.valueOf(request.requiredStage()))
                .requiredLevel(request.requiredLevel())
                .requiredRebirths(request.requiredRebirths())
                .hp(request.hp())
                .atk(request.atk())
                .def(request.def())
                .energyCost(request.energyCost())
                .cooldownMinutes(request.cooldownMinutes())
                .baseXpReward(request.baseXpReward())
                .baseBitsReward(request.baseBitsReward())
                .defeatXpPercent(request.defeatXpPercent() != null ? request.defeatXpPercent() : 10)
                .imageUrl(request.imageUrl())
                .active(true)
                .build();
        return ResponseEntity.ok(bossDefinitionRepository.save(boss));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BossDefinitionEntity> update(@PathVariable Long id, @RequestBody @Valid UpdateBossRequest request) {
        BossDefinitionEntity boss = bossDefinitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Boss not found"));

        if (request.name() != null) boss.setName(request.name());
        if (request.bossType() != null) boss.setBossType(BossType.valueOf(request.bossType()));
        if (request.requiredStage() != null) boss.setRequiredStage(Stage.valueOf(request.requiredStage()));
        if (request.requiredLevel() != null) boss.setRequiredLevel(request.requiredLevel());
        if (request.requiredRebirths() != null) boss.setRequiredRebirths(request.requiredRebirths());
        if (request.hp() != null) boss.setHp(request.hp());
        if (request.atk() != null) boss.setAtk(request.atk());
        if (request.def() != null) boss.setDef(request.def());
        if (request.energyCost() != null) boss.setEnergyCost(request.energyCost());
        if (request.cooldownMinutes() != null) boss.setCooldownMinutes(request.cooldownMinutes());
        if (request.baseXpReward() != null) boss.setBaseXpReward(request.baseXpReward());
        if (request.baseBitsReward() != null) boss.setBaseBitsReward(request.baseBitsReward());
        if (request.defeatXpPercent() != null) boss.setDefeatXpPercent(request.defeatXpPercent());
        if (request.imageUrl() != null) boss.setImageUrl(request.imageUrl());
        if (request.active() != null) boss.setActive(request.active());

        return ResponseEntity.ok(bossDefinitionRepository.save(boss));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        bossDefinitionRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Boss deleted"));
    }

    @PostMapping("/{bossId}/drops")
    public ResponseEntity<BossDropEntity> addDrop(
            @PathVariable Long bossId,
            @RequestBody @Valid CreateBossDropRequest request
    ) {
        BossDefinitionEntity boss = bossDefinitionRepository.findById(bossId)
                .orElseThrow(() -> new NotFoundException("Boss not found"));

        BossDropEntity drop = BossDropEntity.builder()
                .boss(boss)
                .dropType(request.dropType())
                .itemCode(request.itemCode())
                .templateName(request.templateName())
                .equipmentRarity(request.equipmentRarity())
                .chance(request.chance())
                .minQuantity(request.minQuantity())
                .maxQuantity(request.maxQuantity())
                .build();

        return ResponseEntity.ok(bossDropRepository.save(drop));
    }

    @DeleteMapping("/drops/{dropId}")
    public ResponseEntity<Map<String, String>> deleteDrop(@PathVariable Long dropId) {
        bossDropRepository.deleteById(dropId);
        return ResponseEntity.ok(Map.of("message", "Drop deleted"));
    }
}
