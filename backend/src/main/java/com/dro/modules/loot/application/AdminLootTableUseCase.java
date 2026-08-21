package com.dro.modules.loot.application;

import com.dro.modules.inventory.domain.ItemDefinition;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.loot.api.dto.request.LootTableAdminRequest;
import com.dro.modules.loot.api.dto.response.AdminLootItemCatalogResponse;
import com.dro.modules.loot.api.dto.response.AdminLootTableResponse;
import com.dro.modules.loot.domain.LootRarity;
import com.dro.modules.loot.domain.LootTableEntity;
import com.dro.modules.loot.domain.LootTableEntryEntity;
import com.dro.modules.loot.domain.LootTableRarityWeightEntity;
import com.dro.modules.loot.domain.LootTableRules;
import com.dro.modules.loot.infra.LootTableRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.domain.UserType;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.audit.TransactionAuditPublisher;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.ForbiddenException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnauthorizedException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Caso de uso administrativo para manter Loot Tables nomeadas e reutilizáveis.
 *
 * <p>O caso de uso valida o administrador, resolve cada entrada contra o
 * catálogo oficial de itens e persiste pesos e entradas dentro da mesma
 * transação. A pool continua invisível para jogadores comuns.</p>
 */
@Service
@RequiredArgsConstructor
public class AdminLootTableUseCase {

    private final LootTableRepository lootTableRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final PlayerRepository playerRepository;
    private final TransactionAuditPublisher transactionAuditPublisher;

    /** Cria uma Loot Table nova e registra a operação no Outbox. */
    @Transactional
    public AdminLootTableResponse create(String authorization, LootTableAdminRequest request) {
        Player admin = requireAdmin(authorization);
        String code = request.code().trim();
        if (lootTableRepository.findByCode(code).isPresent()) {
            throw new ConflictException("Já existe uma Loot Table com o código " + code + ".");
        }

        PreparedConfiguration configuration = validateAndPrepare(request);
        LootTableEntity entity = new LootTableEntity();
        entity.setCode(code);
        entity.setCreatedBy(admin.getUsername());
        entity.setUpdatedBy(admin.getUsername());
        applyConfiguration(entity, request, configuration);

        LootTableEntity saved = lootTableRepository.save(entity);
        publishAudit("CREATED", saved, admin);
        return toResponse(saved);
    }

    /** Lista tabelas administrativas ordenadas por nome. */
    @Transactional(readOnly = true)
    public List<AdminLootTableResponse> list(String authorization, Boolean activeOnly) {
        requireAdmin(authorization);
        List<LootTableEntity> entities = Boolean.TRUE.equals(activeOnly)
                ? lootTableRepository.findByActiveTrueOrderByNameAsc()
                : lootTableRepository.findAllByOrderByNameAsc();
        return entities.stream().map(this::toResponse).toList();
    }

    /** Retorna uma tabela completa pelo código estável. */
    @Transactional(readOnly = true)
    public AdminLootTableResponse get(String authorization, String code) {
        requireAdmin(authorization);
        return toResponse(findTable(code));
    }

    /** Atualiza metadados, pesos, entradas e status de uma tabela existente. */
    @Transactional
    public AdminLootTableResponse update(
            String authorization,
            String code,
            LootTableAdminRequest request
    ) {
        Player admin = requireAdmin(authorization);
        LootTableEntity entity = findTable(code);
        String normalizedCode = request.code().trim();
        if (!entity.getCode().equals(normalizedCode)) {
            throw new BadRequestException("O código da Loot Table não pode ser alterado.");
        }

        PreparedConfiguration configuration = validateAndPrepare(request);
        applyConfiguration(entity, request, configuration);
        entity.setUpdatedBy(admin.getUsername());
        LootTableEntity saved = lootTableRepository.save(entity);
        publishAudit("UPDATED", saved, admin);
        return toResponse(saved);
    }

    /** Alterna o status ativo sem remover a configuração histórica. */
    @Transactional
    public AdminLootTableResponse toggleActive(String authorization, String code) {
        Player admin = requireAdmin(authorization);
        LootTableEntity entity = findTable(code);
        entity.setActive(!entity.isActive());
        entity.setUpdatedBy(admin.getUsername());
        LootTableEntity saved = lootTableRepository.save(entity);
        publishAudit(saved.isActive() ? "ACTIVATED" : "DEACTIVATED", saved, admin);
        return toResponse(saved);
    }

    /** Retorna itens catalogados para seleção na tela administrativa. */
    @Transactional(readOnly = true)
    public List<AdminLootItemCatalogResponse> catalog(String authorization, String category) {
        requireAdmin(authorization);
        String normalizedCategory = category == null || category.isBlank()
                ? null
                : category.trim().toUpperCase();
        return itemDefinitionRepository.findAll().stream()
                .filter(item -> normalizedCategory == null
                        || normalizedCategory.equalsIgnoreCase(item.getCategory()))
                .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
                .map(AdminLootItemCatalogResponse::from)
                .toList();
    }

    private LootTableEntity findTable(String code) {
        return lootTableRepository.findWithWeightsAndEntriesByCode(code)
                .orElseThrow(() -> new NotFoundException("Loot Table não encontrada: " + code));
    }

    private PreparedConfiguration validateAndPrepare(LootTableAdminRequest request) {
        if (request.rarityWeights() == null || request.rarityWeights().size() != LootRarity.values().length) {
            throw new BadRequestException("Configure exatamente as quatro raridades oficiais.");
        }
        if (request.entries() == null || request.entries().isEmpty()) {
            throw new BadRequestException("A Loot Table precisa ter pelo menos uma entrada.");
        }

        EnumMap<LootRarity, Integer> weights = new EnumMap<>(LootRarity.class);
        for (LootTableAdminRequest.LootTableRarityWeightRequest weight : request.rarityWeights()) {
            if (weight == null || weight.rarity() == null || weight.weight() == null) {
                throw new BadRequestException("Todos os pesos de raridade devem ser preenchidos.");
            }
            if (weights.put(weight.rarity(), weight.weight()) != null) {
                throw new BadRequestException("A raridade " + weight.rarity() + " foi repetida.");
            }
        }
        try {
            LootTableRules.validateRarityWeights(weights);
            LootTableRules.validateItemCount(request.minItems(), request.maxItems());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(exception.getMessage());
        }

        Set<String> duplicateKeys = new HashSet<>();
        List<PreparedEntry> entries = new ArrayList<>();
        for (LootTableAdminRequest.LootTableEntryRequest entry : request.entries()) {
            if (entry == null || entry.rarity() == null || entry.itemType() == null
                    || entry.weight() == null || entry.minQuantity() == null || entry.maxQuantity() == null) {
                throw new BadRequestException("Todas as entradas precisam estar preenchidas.");
            }

            String materialCode = normalizeMaterialCode(entry.materialCode());
            boolean requiresCatalogCode = entry.itemType() == ItemType.EVOLUTION_MATERIAL
                    || entry.itemType() == ItemType.LOOT_CHEST;
            if (requiresCatalogCode && materialCode == null) {
                throw new BadRequestException("Informe o código do material ou baú catalogado.");
            }
            if (!requiresCatalogCode && materialCode != null) {
                throw new BadRequestException("Somente materiais nomeados e baús podem usar código específico.");
            }

            try {
                LootTableRules.validateEntry(
                        entry.itemType(),
                        materialCode,
                        entry.weight(),
                        entry.minQuantity(),
                        entry.maxQuantity()
                );
            } catch (IllegalArgumentException exception) {
                throw new BadRequestException(exception.getMessage());
            }

            String catalogCode = requiresCatalogCode ? materialCode : entry.itemType().name();
            ItemDefinition definition = itemDefinitionRepository.findByCode(catalogCode)
                    .orElseThrow(() -> new ConflictException(
                            "Item não encontrado no catálogo: " + catalogCode));
            validateCatalogCategory(entry.itemType(), definition, catalogCode);
            if (definition.getMaxStack() != null && entry.maxQuantity() > definition.getMaxStack()) {
                throw new BadRequestException(
                        "A quantidade máxima de " + catalogCode + " excede o max_stack do catálogo.");
            }

            String duplicateKey = entry.rarity() + "|" + entry.itemType() + "|" + (materialCode == null ? "" : materialCode);
            if (!duplicateKeys.add(duplicateKey)) {
                throw new BadRequestException("A entrada " + catalogCode + " está duplicada na raridade " + entry.rarity() + ".");
            }

            entries.add(new PreparedEntry(
                    entry.rarity(),
                    entry.itemType(),
                    materialCode,
                    entry.weight(),
                    entry.minQuantity(),
                    entry.maxQuantity(),
                    entry.active() == null || entry.active()
            ));
        }
        if (entries.size() < request.minItems()) {
            throw new BadRequestException(
                    "A Loot Table precisa ter pelo menos " + request.minItems() + " entradas distintas para atender ao mínimo configurado.");
        }
        return new PreparedConfiguration(weights, entries);
    }

    private void validateCatalogCategory(ItemType itemType, ItemDefinition definition, String catalogCode) {
        if (itemType == ItemType.EVOLUTION_MATERIAL
                && !"EVOLUTION_MATERIAL".equalsIgnoreCase(definition.getCategory())) {
            throw new ConflictException("O item " + catalogCode + " não é um material de evolução catalogado.");
        }
        if (itemType == ItemType.LOOT_CHEST
                && !"CHEST".equalsIgnoreCase(definition.getCategory())) {
            throw new ConflictException("O item " + catalogCode + " não é um baú catalogado.");
        }
    }

    private void applyConfiguration(
            LootTableEntity entity,
            LootTableAdminRequest request,
            PreparedConfiguration configuration
    ) {
        entity.setName(request.name().trim());
        entity.setDescription(normalizeDescription(request.description()));
        entity.setMinItems(request.minItems());
        entity.setMaxItems(request.maxItems());
        entity.setActive(request.active() == null || request.active());

        entity.getRarityWeights().clear();
        configuration.weights().forEach((rarity, weight) ->
                entity.getRarityWeights().add(LootTableRarityWeightEntity.builder()
                        .lootTable(entity)
                        .rarity(rarity)
                        .weight(weight)
                        .build()));

        entity.getEntries().clear();
        configuration.entries().forEach(entry ->
                entity.getEntries().add(LootTableEntryEntity.builder()
                        .lootTable(entity)
                        .rarity(entry.rarity())
                        .itemType(entry.itemType())
                        .materialCode(entry.materialCode())
                        .weight(entry.weight())
                        .minQuantity(entry.minQuantity())
                        .maxQuantity(entry.maxQuantity())
                        .active(entry.active())
                        .build()));
    }

    private AdminLootTableResponse toResponse(LootTableEntity entity) {
        Map<String, ItemDefinition> catalog = new HashMap<>();
        entity.getEntries().forEach(entry -> {
            String code = entry.getMaterialCode() != null
                    ? entry.getMaterialCode()
                    : entry.getItemType().name();
            itemDefinitionRepository.findByCode(code).ifPresent(definition -> catalog.put(code, definition));
        });
        return AdminLootTableResponse.from(entity, catalog);
    }

    private void publishAudit(String operation, LootTableEntity entity, Player admin) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("module", "loot");
        payload.put("operation", operation.toLowerCase());
        payload.put("adminId", admin.getId().toString());
        payload.put("adminUsername", admin.getUsername());
        payload.put("lootTableCode", entity.getCode());
        payload.put("active", entity.isActive());
        payload.put("rarityWeightCount", entity.getRarityWeights().size());
        payload.put("entryCount", entity.getEntries().size());

        transactionAuditPublisher.success(
                "admin-loot-table:" + entity.getCode() + ":" + operation.toLowerCase() + ":" + UUID.randomUUID(),
                "ADMIN_LOOT_TABLE_" + operation,
                "LootTable",
                entity.getCode(),
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
            throw new ForbiddenException("Somente administradores podem configurar Loot Tables.");
        }
        return admin;
    }

    private String normalizeMaterialCode(String code) {
        if (code == null || code.isBlank()) return null;
        return code.trim().toUpperCase();
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) return null;
        return description.trim();
    }

    private record PreparedConfiguration(
            Map<LootRarity, Integer> weights,
            List<PreparedEntry> entries
    ) {
    }

    private record PreparedEntry(
            LootRarity rarity,
            ItemType itemType,
            String materialCode,
            int weight,
            int minQuantity,
            int maxQuantity,
            boolean active
    ) {
    }
}
