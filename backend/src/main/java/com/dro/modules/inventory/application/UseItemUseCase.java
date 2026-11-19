package com.dro.modules.inventory.application;

import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.incubation.domain.IncubatorRules;
import com.dro.modules.mission.domain.MissionSlotRules;
import com.dro.modules.mission.domain.TeamSlotRules;
import com.dro.modules.inventory.api.dto.response.UseItemResponse;
import com.dro.modules.inventory.api.dto.response.TeamEnergyResponse;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.domain.ItemType;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.mission.application.NewlyUnlockedContentService;
import com.dro.modules.mission.domain.MissionTeam;
import com.dro.modules.mission.infra.MissionTeamRepository;
import com.dro.modules.mission.api.dto.response.NewlyUnlockedContentResponse;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.ConflictException;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.exception.UnprocessableException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Inventário.
 */
@Service
public class UseItemUseCase {
    private static final int GENERIC_ITEM_XP = 50;
    private static final int MAX_BATCH_QUANTITY = 999;
    private final InventoryRepository inventoryRepository;
    private final MissionTeamRepository missionTeamRepository;
    private final DigimonRepository digimonRepository;
    private final PlayerRepository playerRepository;
    private final ClanBonusService clanBonusService;
    private final NewlyUnlockedContentService newlyUnlockedContentService;

    @Transactional
    public UseItemResponse execute(String token, ItemType type) {
        return execute(token, type, null);
    }

    @Transactional
    public UseItemResponse execute(String token, ItemType type, Integer requestedQuantity) {
        if (type == null) {
            throw new BadRequestException("Item type is required");
        }
        if (isIncubationOnly(type)) {
            throw new BadRequestException("Digitamas e incubadoras devem ser usados pela tela de incubação");
        }
        if (type == ItemType.RARITY_PRESERVATION) {
            throw new BadRequestException("O Cristal de Preservação deve ser usado na tela de Renascimento");
        }
        UUID playerId = TokenExtractor.extractPlayerId(token);
        if (type == ItemType.VIP_DAY_PASS) {
            return useVipPass(playerId, requestedQuantity);
        }
        if (type == ItemType.INCUBATION_SLOT_UNLOCK) {
            return unlockIncubationSlot(playerId);
        }
        if (type == ItemType.MISSION_SLOT_UNLOCK) {
            return unlockMissionSlot(playerId);
        }
        if (type == ItemType.TEAM_SLOT_UNLOCK) {
            return unlockTeamSlot(playerId);
        }

        boolean batchUsableItem = isBatchUsableItem(type);
        boolean xpDisk = isXpDisk(type);
        int quantity = resolveQuantity(batchUsableItem, requestedQuantity);
        Player player = playerRepository.findByIdForUpdate(playerId).orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }
        Digimon digimon = digimonRepository.findByIdForUpdate(player.getActiveDigimonId()).orElseThrow(() -> new NotFoundException("Active digimon not found"));
        InventoryItem item = inventoryRepository.findByPlayerIdAndItemTypeForUpdate(playerId, type).orElseThrow(() -> new NotFoundException("Item not found"));
        if (item.getQuantity() <= 0) {
            throw new UnprocessableException("No item available");
        }
        if (item.getQuantity() < quantity) {
            throw new UnprocessableException("Not enough items in inventory");
        }

        if (type == ItemType.ENERGY_CELL || type == ItemType.VIP_ENERGY_CHARGE) {
            return useEnergyItem(player, digimon, item, type);
        }

        int storageExpansion = storageExpansionAmount(type);
        if (storageExpansion > 0) {
            int totalStorageExpansion = storageExpansion * quantity;
            consume(item, quantity);
            player.setMaxStorageSlots(player.getMaxStorageSlots() + totalStorageExpansion);
            playerRepository.save(player);
            return new UseItemResponse(
                    type,
                    quantity,
                    0,
                    digimon.getLevel(),
                    digimon.getLevel(),
                    false,
                    "Storage expandido em +" + totalStorageExpansion + " espaço(s)!",
                    NewlyUnlockedContentResponse.empty()
            );
        }

        int previousLevel = digimon.getLevel();
        Stage previousStage = digimon.getStage();
        int xpGranted = 0;
        if (xpDisk) {
            int percentage = xpDiskPercentage(type);
            for (int index = 0; index < quantity; index++) {
                int xpToNextLevel = digimon.getExperienceToNextLevel();
                if (xpToNextLevel <= 0) {
                    throw new BadRequestException("Este Digimon já está no nível máximo para a quantidade solicitada");
                }
                int diskXp = calculateXpDiskAmount(xpToNextLevel, percentage);
                digimon.grantDirectExperience(diskXp);
                xpGranted += diskXp;
            }
        } else {
            xpGranted = GENERIC_ITEM_XP * quantity;
            for (int index = 0; index < quantity; index++) {
                digimon.gainExperience(GENERIC_ITEM_XP);
            }
        }

        consume(item, quantity);
        digimonRepository.save(digimon);
        String message = batchUsableItem && quantity > 1
                ? "Itens utilizados com sucesso."
                : "Item utilizado com sucesso.";
        NewlyUnlockedContentResponse newlyUnlockedContent = newlyUnlockedContentService == null
                ? NewlyUnlockedContentResponse.empty()
                : newlyUnlockedContentService.detect(digimon, previousLevel, previousStage);
        return new UseItemResponse(
                type,
                quantity,
                xpGranted,
                previousLevel,
                digimon.getLevel(),
                digimon.getLevel() > previousLevel,
                message,
                newlyUnlockedContent
        );
    }

    private UseItemResponse useVipPass(UUID playerId, Integer requestedQuantity) {
        if (requestedQuantity != null && requestedQuantity != 1) {
            throw new BadRequestException("Utilize um Passe VIP por vez.");
        }
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Jogador não encontrado."));
        InventoryItem item = inventoryRepository.findByPlayerIdAndItemTypeForUpdate(playerId, ItemType.VIP_DAY_PASS)
                .filter(entry -> entry.getQuantity() > 0)
                .orElseThrow(() -> new UnprocessableException("Você não tem um Passe VIP disponível."));
        Instant expiry = com.dro.modules.player.domain.VipPassRules.extendOneDay(player.getVipExpiresAt(), Instant.now());
        consume(item, 1);
        player.setVipExpiresAt(expiry);
        playerRepository.save(player);
        return new UseItemResponse(ItemType.VIP_DAY_PASS, 1, 0, 0, 0, false,
                "Passe utilizado! Mais 24 horas de VIP. Consulte a validade na página VIP.",
                NewlyUnlockedContentResponse.empty());
    }

    private UseItemResponse useEnergyItem(Player player, Digimon digimon, InventoryItem item, ItemType type) {
        if (!player.getId().equals(digimon.getPlayerId())) {
            throw new BadRequestException("O Digimon ativo não pertence a este jogador.");
        }
        int recovered = restoreEnergy(digimon, maxEnergyBonus(player), type);
        if (recovered == 0) {
            throw new UnprocessableException("O Digimon ativo já está com a energia cheia.");
        }
        consume(item, 1);
        digimonRepository.save(digimon);
        String itemName = type == ItemType.VIP_ENERGY_CHARGE ? "Carga de Energia Total" : "Célula de Energia";
        return new UseItemResponse(type, 1, 0, digimon.getLevel(), digimon.getLevel(),
                false, itemName + ": +" + recovered + " de energia.", NewlyUnlockedContentResponse.empty());
    }

    @Transactional(readOnly = true)
    public TeamEnergyResponse previewTeamEnergy(String token, UUID teamId) {
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Jogador não encontrado"));
        MissionTeam team = missionTeamRepository.findByIdAndPlayerId(teamId, playerId)
                .orElseThrow(() -> new NotFoundException("Time não encontrado"));
        List<Digimon> members = validateTeamMembers(team, digimonRepository.findAllById(team.getDigimonIds()));
        int bonus = maxEnergyBonus(player);
        var energy = members.stream().map(digimon -> {
            // Calculate passive regeneration on a snapshot so the preview never changes stored energy.
            Digimon snapshot = Digimon.builder()
                    .energy(digimon.getEnergy())
                    .maxEnergy(digimon.getMaxEnergy())
                    .lastEnergyUpdate(digimon.getLastEnergyUpdate())
                    .build();
            snapshot.regenerateEnergy(bonus);
            int capacity = snapshot.getMaxEnergy() + bonus;
            int current = snapshot.getEnergy();
            return new TeamEnergyResponse.Member(digimon.getId(), digimon.getName(), current, capacity,
                    recoveryAmount(ItemType.ENERGY_CELL, capacity, current),
                    recoveryAmount(ItemType.VIP_ENERGY_CHARGE, capacity, current));
        }).toList();
        return new TeamEnergyResponse(team.getName(), energy,
                availableQuantity(playerId, ItemType.ENERGY_CELL),
                availableQuantity(playerId, ItemType.VIP_ENERGY_CHARGE));
    }

    @Transactional
    public int useEnergyOnTeam(String token, UUID teamId, ItemType type, List<UUID> expectedMembers) {
        if (type != ItemType.ENERGY_CELL && type != ItemType.VIP_ENERGY_CHARGE) {
            throw new BadRequestException("Selecione um item de recuperação de energia.");
        }
        UUID playerId = TokenExtractor.extractPlayerId(token);
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Jogador não encontrado"));
        MissionTeam team = missionTeamRepository.findByIdAndPlayerIdForUpdate(teamId, playerId)
                .orElseThrow(() -> new NotFoundException("Time não encontrado"));
        List<UUID> memberIds = team.getDigimonIds();
        if (!memberIds.equals(expectedMembers)) {
            throw new ConflictException("A formação mudou. Abra a recarga do time novamente.");
        }
        List<Digimon> members = validateTeamMembers(team, digimonRepository.findAllByIdForUpdate(playerId, memberIds));
        InventoryItem item = inventoryRepository.findByPlayerIdAndItemTypeForUpdate(playerId, type)
                .filter(entry -> entry.getQuantity() > 0)
                .orElseThrow(() -> new UnprocessableException("Você não tem esse item no inventário."));
        int bonus = maxEnergyBonus(player);
        int totalRecovered = members.stream().mapToInt(digimon -> restoreEnergy(digimon, bonus, type)).sum();
        if (totalRecovered == 0) {
            throw new UnprocessableException("Todos os Digimons do time já estão com energia cheia.");
        }
        consume(item, 1);
        digimonRepository.saveAll(members);
        return totalRecovered;
    }

    private List<Digimon> validateTeamMembers(MissionTeam team, List<Digimon> digimons) {
        return team.getDigimonIds().stream().map(id -> digimons.stream()
                .filter(digimon -> id.equals(digimon.getId()) && team.getPlayerId().equals(digimon.getPlayerId()))
                .filter(digimon -> digimon.getStatus() == DigimonStatus.ACTIVE
                        || digimon.getStatus() == DigimonStatus.HATCHED || digimon.getStatus() == DigimonStatus.STORED)
                .findFirst()
                .orElseThrow(() -> new ConflictException("Um ou mais Digimons do time não estão disponíveis.")))
                .toList();
    }

    private int availableQuantity(UUID playerId, ItemType type) {
        return inventoryRepository.findByPlayerIdAndItemType(playerId, type).map(InventoryItem::getQuantity).orElse(0);
    }

    private int maxEnergyBonus(Player player) {
        return player.getClanId() == null ? 0 : clanBonusService.getMaxEnergyBonus(player.getClanId());
    }

    private static int recoveryAmount(ItemType type, int capacity, int current) {
        int missing = Math.max(0, capacity - current);
        return type == ItemType.VIP_ENERGY_CHARGE ? missing : Math.min(missing, (capacity + 9) / 10);
    }

    private int restoreEnergy(Digimon digimon, int bonus, ItemType type) {
        digimon.regenerateEnergy(bonus);
        int capacity = digimon.getMaxEnergy() + bonus;
        int recovered = recoveryAmount(type, capacity, digimon.getEnergy());
        if (recovered > 0) {
            digimon.setEnergy(digimon.getEnergy() + recovered);
            if (digimon.getEnergy() == capacity) {
                digimon.setLastEnergyUpdate(Instant.now());
            }
        }
        return recovered;
    }

    private int resolveQuantity(boolean batchUsableItem, Integer requestedQuantity) {
        if (!batchUsableItem) {
            return 1;
        }
        int quantity = requestedQuantity == null ? 1 : requestedQuantity;
        if (quantity < 1 || quantity > MAX_BATCH_QUANTITY) {
            throw new BadRequestException("A quantidade do item deve estar entre 1 e " + MAX_BATCH_QUANTITY);
        }
        return quantity;
    }

    private int storageExpansionAmount(ItemType type) {
        return switch (type) {
            case STORAGE_SLOT_1 -> 1;
            case STORAGE_SLOT_5 -> 5;
            case STORAGE_SLOT_10 -> 10;
            default -> 0;
        };
    }

    private int calculateXpDiskAmount(int xpToNextLevel, int percentage) {
        return Math.max(1, (int) Math.floor((double) xpToNextLevel * percentage / 100.0));
    }

    private void consume(InventoryItem item, int quantity) {
        int remaining = item.getQuantity() - quantity;
        item.setQuantity(remaining);
        if (remaining == 0) {
            inventoryRepository.delete(item);
        } else {
            inventoryRepository.save(item);
        }
    }

    private UseItemResponse unlockIncubationSlot(UUID playerId) {
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getUnlockedIncubationSlots() >= IncubatorRules.TOTAL_SLOTS) {
            throw new BadRequestException("Todos os slots de incubação já estão desbloqueados");
        }
        if (player.getActiveDigimonId() == null) {
            throw new BadRequestException("No active digimon selected");
        }

        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElseThrow(() -> new NotFoundException("Active digimon not found"));
        InventoryItem item = inventoryRepository
                .findByPlayerIdAndItemTypeForUpdate(playerId, ItemType.INCUBATION_SLOT_UNLOCK)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        if (item.getQuantity() <= 0) {
            throw new UnprocessableException("No item available");
        }

        consume(item, 1);
        player.setUnlockedIncubationSlots(player.getUnlockedIncubationSlots() + 1);
        playerRepository.save(player);
        return new UseItemResponse(
                ItemType.INCUBATION_SLOT_UNLOCK,
                1,
                0,
                digimon.getLevel(),
                digimon.getLevel(),
                false,
                "Slot de incubação desbloqueado!",
                NewlyUnlockedContentResponse.empty()
        );
    }

    private UseItemResponse unlockMissionSlot(UUID playerId) {
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getUnlockedMissionSlots() >= MissionSlotRules.TOTAL_SLOTS) {
            throw new BadRequestException("Todos os slots de missão já estão desbloqueados");
        }

        InventoryItem item = inventoryRepository
                .findByPlayerIdAndItemTypeForUpdate(playerId, ItemType.MISSION_SLOT_UNLOCK)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        if (item.getQuantity() <= 0) {
            throw new UnprocessableException("No item available");
        }

        consume(item, 1);
        player.setUnlockedMissionSlots(player.getUnlockedMissionSlots() + 1);
        playerRepository.save(player);
        return new UseItemResponse(
                ItemType.MISSION_SLOT_UNLOCK,
                1,
                0,
                0,
                0,
                false,
                "Slot de missão desbloqueado!",
                NewlyUnlockedContentResponse.empty()
        );
    }

    private UseItemResponse unlockTeamSlot(UUID playerId) {
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));
        if (player.getMaxTeamSlots() >= TeamSlotRules.MAX_SLOTS) {
            throw new BadRequestException("Todos os slots de times já estão desbloqueados");
        }

        InventoryItem item = inventoryRepository
                .findByPlayerIdAndItemTypeForUpdate(playerId, ItemType.TEAM_SLOT_UNLOCK)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        if (item.getQuantity() <= 0) {
            throw new UnprocessableException("No item available");
        }

        consume(item, 1);
        player.setMaxTeamSlots(Math.min(TeamSlotRules.MAX_SLOTS, player.getMaxTeamSlots() + 1));
        playerRepository.save(player);
        return new UseItemResponse(
                ItemType.TEAM_SLOT_UNLOCK,
                1,
                0,
                0,
                0,
                false,
                "Slot de time desbloqueado!",
                NewlyUnlockedContentResponse.empty()
        );
    }

    private boolean isBatchUsableItem(ItemType type) {
        return isXpDisk(type)
                || type == ItemType.POTION_SMALL
                || type == ItemType.TRAINING_STONE
                || type == ItemType.DATA_CORE
                || storageExpansionAmount(type) > 0;
    }

    private boolean isXpDisk(ItemType type) {
        return switch (type) {
            case XP_DISC_1, XP_DISC_3, XP_DISC_5, XP_DISC_10, XP_DISC_15, XP_DISC_20 -> true;
            default -> false;
        };
    }

    private int xpDiskPercentage(ItemType type) {
        return switch (type) {
            case XP_DISC_1 -> 1;
            case XP_DISC_3 -> 3;
            case XP_DISC_5 -> 5;
            case XP_DISC_10 -> 10;
            case XP_DISC_15 -> 15;
            case XP_DISC_20 -> 20;
            default -> throw new IllegalArgumentException("Item is not an XP disk: " + type);
        };
    }

    private boolean isIncubationOnly(ItemType type) {
        return switch (type) {
            case DIGITAMA_STARTER, DIGITAMA_FIRE, DIGITAMA_WATER, DIGITAMA_NATURE,
                 DIGITAMA_EARTH, DIGITAMA_WIND, DIGITAMA_LIGHT, DIGITAMA_DARK,
                 DIGITAMA_THUNDER, DIGITAMA_NEUTRAL, DIGITAMA_ICE, DIGITAMA_STEEL,
                 INCUBATOR_COMMON, INCUBATOR_RARE, INCUBATOR_EPIC -> true;
            default -> false;
        };
    }

    public UseItemUseCase(final InventoryRepository inventoryRepository, final DigimonRepository digimonRepository, final PlayerRepository playerRepository) {
        this(inventoryRepository, digimonRepository, playerRepository, null, null, null);
    }

    @Autowired
    public UseItemUseCase(final InventoryRepository inventoryRepository, final DigimonRepository digimonRepository, final PlayerRepository playerRepository, final NewlyUnlockedContentService newlyUnlockedContentService, final ClanBonusService clanBonusService, final MissionTeamRepository missionTeamRepository) {
        this.missionTeamRepository = missionTeamRepository;
        this.clanBonusService = clanBonusService;
        this.inventoryRepository = inventoryRepository;
        this.digimonRepository = digimonRepository;
        this.playerRepository = playerRepository;
        this.newlyUnlockedContentService = newlyUnlockedContentService;
    }
}
