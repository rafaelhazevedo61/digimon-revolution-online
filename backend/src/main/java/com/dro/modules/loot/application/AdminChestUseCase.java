package com.dro.modules.loot.application;

import com.dro.modules.boss.infra.BossDefinitionRepository;
import com.dro.modules.loot.api.dto.request.AdminChestUpdateRequest;
import com.dro.modules.loot.api.dto.response.AdminChestResponse;
import com.dro.modules.loot.domain.ChestDefinitionEntity;
import com.dro.modules.loot.domain.LootTableEntity;
import com.dro.modules.loot.infra.ChestDefinitionRepository;
import com.dro.modules.loot.infra.LootTableRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnauthorizedException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso administrativo para vincular Baús temáticos às Loot Tables nomeadas.
 */
@Service
@RequiredArgsConstructor
public class AdminChestUseCase {

    private final ChestDefinitionRepository chestDefinitionRepository;
    private final BossDefinitionRepository bossDefinitionRepository;
    private final LootTableRepository lootTableRepository;
    private final PlayerRepository playerRepository;
    private final TransactionAuditPublisher transactionAuditPublisher;

    /** Lista os baús, podendo restringir aos ativos. */
    @Transactional(readOnly = true)
    public List<AdminChestResponse> list(String authorization, Boolean activeOnly) {
        requireAdmin(authorization);
        List<ChestDefinitionEntity> chests = Boolean.TRUE.equals(activeOnly)
                ? chestDefinitionRepository.findByActiveTrueOrderByNameAsc()
                : chestDefinitionRepository.findAllByOrderByNameAsc();
        return chests.stream().map(AdminChestResponse::from).toList();
    }

    /** Consulta um baú pelo código estável. */
    @Transactional(readOnly = true)
    public AdminChestResponse get(String authorization, String code) {
        requireAdmin(authorization);
        return AdminChestResponse.from(findChest(code));
    }

    /** Atualiza dados editáveis e o vínculo para uma Loot Table ativa. */
    @Transactional
    public AdminChestResponse update(
            String authorization,
            String code,
            AdminChestUpdateRequest request
    ) {
        Player admin = requireAdmin(authorization);
        ChestDefinitionEntity chest = findChest(code);
        LootTableEntity lootTable = resolveActiveLootTable(request.lootTableCode());

        chest.setName(request.name().trim());
        chest.setDescription(normalize(request.description()));
        chest.setIcon(normalize(request.icon()));
        chest.setLootTable(lootTable);
        if (Boolean.FALSE.equals(request.active()) && chest.isActive()) {
            ensureNotLinkedToBoss(chest);
        }
        if (request.tradable() != null) chest.setTradable(request.tradable());
        if (request.active() != null) chest.setActive(request.active());
        chest.setUpdatedBy(admin.getUsername());

        ChestDefinitionEntity saved = chestDefinitionRepository.saveAndFlush(chest);
        publishAudit("UPDATED", saved, admin, lootTable.getCode());
        return AdminChestResponse.from(saved);
    }

    /** Ativa ou desativa o baú sem apagar o vínculo ou o histórico. */
    @Transactional
    public AdminChestResponse toggleActive(String authorization, String code) {
        Player admin = requireAdmin(authorization);
        ChestDefinitionEntity chest = findChest(code);
        if (!chest.isActive()) {
            ensureActiveLootTable(chest.getLootTable());
        } else {
            ensureNotLinkedToBoss(chest);
        }
        chest.setActive(!chest.isActive());
        chest.setUpdatedBy(admin.getUsername());
        ChestDefinitionEntity saved = chestDefinitionRepository.saveAndFlush(chest);
        publishAudit(saved.isActive() ? "ACTIVATED" : "DEACTIVATED", saved,
                admin, saved.getLootTable() != null ? saved.getLootTable().getCode() : null);
        return AdminChestResponse.from(saved);
    }

    private ChestDefinitionEntity findChest(String code) {
        return chestDefinitionRepository.findWithCatalogByCode(code)
                .orElseThrow(() -> new NotFoundException("Baú da Área não encontrado: " + code));
    }

    private LootTableEntity resolveActiveLootTable(String code) {
        String normalizedCode = code == null ? "" : code.trim();
        return lootTableRepository.findByCodeAndActiveTrue(normalizedCode)
                .orElseThrow(() -> new ConflictException(
                        "Loot Table ativa não encontrada: " + normalizedCode));
    }

    private void ensureActiveLootTable(LootTableEntity lootTable) {
        if (lootTable == null || !lootTable.isActive()) {
            throw new ConflictException("Não é possível ativar um baú sem Loot Table ativa.");
        }
    }

    private void ensureNotLinkedToBoss(ChestDefinitionEntity chest) {
        if (bossDefinitionRepository.existsByChestDefinition_Id(chest.getId())) {
            throw new ConflictException(
                    "Não é possível desativar o Baú porque ele está vinculado a um ou mais Bosses."
            );
        }
    }

    private void publishAudit(String operation, ChestDefinitionEntity chest, Player admin, String lootTableCode) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("module", "loot");
        payload.put("operation", operation.toLowerCase());
        payload.put("adminId", admin.getId().toString());
        payload.put("adminUsername", admin.getUsername());
        payload.put("chestCode", chest.getCode());
        payload.put("lootTableCode", lootTableCode);
        payload.put("active", chest.isActive());
        payload.put("tradable", chest.isTradable());

        transactionAuditPublisher.success(
                "admin-chest:" + chest.getCode() + ":" + operation.toLowerCase() + ":" + UUID.randomUUID(),
                "ADMIN_CHEST_" + operation,
                "ChestDefinition",
                chest.getCode(),
                payload
        );
    }

    private Player requireAdmin(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new UnauthorizedException("Authorization é obrigatório.");
        }
        UUID adminId = TokenExtractor.extractPlayerId(authorization);
        Player admin = playerRepository.findById(adminId)
                .orElseThrow(() -> new UnauthorizedException("Administrador não encontrado."));
        if (admin.getUserType() != UserType.ADMIN) {
            throw new ForbiddenException("Somente administradores podem configurar Baús da Área.");
        }
        return admin;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
