package com.dro.modules.player.application;

import com.dro.modules.digimon.api.dto.response.DigimonResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.api.dto.response.DigimonEquipmentResponse;
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
import com.dro.modules.mission.domain.MissionDefinitionEntity;
import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.player.api.dto.response.ActiveMissionResponse;
import com.dro.modules.player.api.dto.response.InventorySummaryResponse;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.player.api.dto.response.PlayerDashboardResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPlayerDashboardUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final DigimonInfosRepository digimonInfosRepository;
    private final EquipmentRepository equipmentRepository;
    private final InventoryRepository inventoryRepository;
    private final MissionInstanceRepository missionInstanceRepository;
    private final IncubationRepository incubationRepository;
    private final MissionDefinitionRepository missionDefinitionRepository;

    public PlayerDashboardResponse execute(String token) {

        UUID playerId = TokenExtractor.extractPlayerId(token);

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException("Player not found"));

        DigimonResponse activeDigimon = buildActiveDigimon(player);

        List<Equipment> equippedDomain = buildEquippedDomainItems(player);
        List<EquipmentResponse> equippedItems = equippedDomain.stream()
                .map(EquipmentResponse::from).toList();

        var setInfo = EquipmentRules.getSetBonusInfo(equippedDomain);
        var setBonus = new DigimonEquipmentResponse.SetBonusResponse(
                setInfo.setCode(), setInfo.pieceCount(),
                setInfo.bonusHpPercent(), setInfo.bonusAtkPercent(), setInfo.bonusDefPercent());

        List<InventorySummaryResponse> inventory = buildInventory(player);

        List<ActiveMissionResponse> activeMissions = buildActiveMissions(playerId);

        IncubationResponse incubation = buildIncubation(playerId);

        long activeCount = digimonRepository.countByPlayerIdAndStatus(playerId, DigimonStatus.ACTIVE);
        long storedCount = digimonRepository.countByPlayerIdAndStatus(playerId, DigimonStatus.STORED);
        var slotInfo = new PlayerDashboardResponse.SlotInfoResponse(
                (int) activeCount, player.getMaxDigimonSlots(),
                (int) storedCount, player.getMaxStorageSlots()
        );

        return new PlayerDashboardResponse(
                player.getId(),
                player.getUsername(),
                player.getEmail(),
                player.getCreatedAt(),
                activeDigimon,
                equippedItems,
                setBonus,
                inventory,
                activeMissions,
                incubation,
                slotInfo
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

        DigimonInfos info = d.getDigimonInfoId() != null
                ? digimonInfosRepository.findById(d.getDigimonInfoId()).orElse(null)
                : null;

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
                d.getGrade(),
                d.getRarity(),
                d.getPersonality(),
                d.getTrait(),
                d.getEnergy(),
                d.getMaxEnergy(),
                d.getBits(),
                d.getRebirthCount(),
                d.getRebornedFrom(),
                d.getStatus(),
                d.getDigimonInfoId(),
                info != null ? info.getAttribute().name() : null,
                info != null ? info.getElement().name() : null,
                EquipmentRules.totalBonusHp(equipped),
                EquipmentRules.totalBonusAttack(equipped),
                EquipmentRules.totalBonusDefense(equipped)
        );
    }

    private List<Equipment> buildEquippedDomainItems(Player player) {

        if (player.getActiveDigimonId() == null) {
            return List.of();
        }

        Digimon digimon = digimonRepository.findById(player.getActiveDigimonId())
                .orElse(null);

        if (digimon == null) {
            return List.of();
        }

        return getEquippedItems(digimon);
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

                    String missionName = missionDefinitionRepository.findById(instance.getMissionId())
                            .map(MissionDefinitionEntity::getName)
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

        List<Incubation> incubations = incubationRepository
                .findByPlayerIdAndStatusNot(playerId, IncubationStatus.CLAIMED);

        if (incubations == null || incubations.isEmpty()) {
            return null;
        }

        // Seleciona a incubação mais próxima de terminar
        Incubation incubation = incubations.stream()
                .min(Comparator.comparing(Incubation::getFinishAt))
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
            // Só chama markReadyIfFinished se o status for IN_PROGRESS
            if (incubation.getStatus() == IncubationStatus.IN_PROGRESS) {
                incubation.markReadyIfFinished();
                incubationRepository.save(incubation);
            }
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
