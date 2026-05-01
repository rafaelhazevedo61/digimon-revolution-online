package com.dro.modules.player.application;

import com.dro.modules.digimon.api.dto.response.DigimonResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.api.dto.response.EquipmentResponse;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.incubation.api.IncubationResponse;
import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.mission.domain.MissionCatalog;
import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.player.api.dto.ActiveMissionResponse;
import com.dro.modules.player.api.dto.InventorySummaryResponse;
import com.dro.modules.player.api.dto.PlayerDashboardResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.BadRequestException;
import com.dro.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPlayerDashboardUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final EquipmentRepository equipmentRepository;
    private final InventoryRepository inventoryRepository;
    private final MissionInstanceRepository missionInstanceRepository;
    private final IncubationRepository incubationRepository;

    public PlayerDashboardResponse execute(String token) {

        if (token == null || !token.contains(":")) {
            throw new BadRequestException("Invalid token");
        }

        UUID playerId = UUID.fromString(token.split(":")[1]);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        DigimonResponse activeDigimon = buildActiveDigimon(player);

        List<EquipmentResponse> equippedItems = buildEquippedItems(player);

        List<InventorySummaryResponse> inventory = buildInventory(player);

        List<ActiveMissionResponse> activeMissions = buildActiveMissions(playerId);

        IncubationResponse incubation = buildIncubation(playerId);

        return new PlayerDashboardResponse(
                player.getId(),
                player.getUsername(),
                player.getEmail(),
                player.getCreatedAt(),
                activeDigimon,
                equippedItems,
                inventory,
                activeMissions,
                incubation
        );
    }

    private DigimonResponse buildActiveDigimon(Player player) {

        if (player.getActiveDigimonId() == null) {
            return null;
        }

        Digimon d = digimonRepository.findById(player.getActiveDigimonId())
                .orElse(null);

        if (d == null) {
            return null;
        }

        d.regenerateEnergy();
        digimonRepository.save(d);

        List<Equipment> equipped = getEquippedItems(d);

        return new DigimonResponse(
                d.getId(),
                d.getName(),
                d.getType(),
                d.getStage(),
                d.getLevel(),
                d.getExperience(),
                d.getHp(),
                d.getAttack(),
                d.getDefense(),
                d.getIvHp(),
                d.getIvAttack(),
                d.getIvDefense(),
                d.getRarity(),
                d.getPersonality(),
                d.getTrait(),
                d.getEnergy(),
                d.getMaxEnergy(),
                d.getBits(),
                d.getRebirthCount(),
                d.getRebornedFrom(),
                d.getStatus(),
                EquipmentRules.totalBonusHp(equipped),
                EquipmentRules.totalBonusAttack(equipped),
                EquipmentRules.totalBonusDefense(equipped)
        );
    }

    private List<EquipmentResponse> buildEquippedItems(Player player) {

        if (player.getActiveDigimonId() == null) {
            return List.of();
        }

        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElse(null);

        if (digimon == null) {
            return List.of();
        }

        return getEquippedItems(digimon).stream()
                .map(EquipmentResponse::from)
                .toList();
    }

    private List<InventorySummaryResponse> buildInventory(Player player) {

        if (player.getActiveDigimonId() == null) {
            return List.of();
        }

        List<InventoryItem> items = inventoryRepository.findByDigimonId(player.getActiveDigimonId());

        return items.stream()
                .filter(item -> item.getQuantity() > 0)
                .map(item -> new InventorySummaryResponse(
                        item.getItemType(),
                        item.getQuantity()
                ))
                .toList();
    }

    private List<ActiveMissionResponse> buildActiveMissions(UUID playerId) {

        List<MissionInstance> missions = missionInstanceRepository
                .findByPlayerIdAndStatusIn(playerId, List.of(MissionStatus.RUNNING, MissionStatus.COMPLETED));

        return missions.stream()
                .map(instance -> {
                    if (instance.updateStatusIfFinished()) {
                        missionInstanceRepository.save(instance);
                    }

                    String missionName = MissionCatalog.findById(instance.getMissionId())
                            .map(m -> m.getName())
                            .orElse(instance.getMissionId());

                    long remaining = Duration.between(Instant.now(), instance.getEndsAt()).getSeconds();
                    if (remaining < 0) remaining = 0;

                    return new ActiveMissionResponse(
                            instance.getId(),
                            instance.getMissionId(),
                            missionName,
                            instance.getStatus(),
                            instance.getStartedAt(),
                            instance.getEndsAt(),
                            remaining
                    );
                })
                .toList();
    }

    private IncubationResponse buildIncubation(UUID playerId) {

        Incubation incubation = incubationRepository
                .findByPlayerIdAndStatus(playerId, IncubationStatus.IN_PROGRESS)
                .orElse(null);

        if (incubation == null) {
            return null;
        }

        long remaining = Duration.between(
                LocalDateTime.now(),
                incubation.getFinishAt()
        ).getSeconds();

        if (remaining < 0) {
            remaining = 0;
        }

        return new IncubationResponse(
                incubation.getDigitamaType(),
                incubation.getIncubatorType(),
                incubation.getStatus(),
                incubation.getStartedAt(),
                incubation.getFinishAt(),
                remaining
        );
    }

    private List<Equipment> getEquippedItems(Digimon digimon) {
        List<Equipment> equipped = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            UUID equipId = digimon.getEquipmentIdBySlot(slot);
            if (equipId != null) {
                equipmentRepository.findById(equipId).ifPresent(equipped::add);
            }
        }

        return equipped;
    }
}
