package com.dro.modules.player.application;

import com.dro.modules.digimon.api.dto.response.DigimonResponse;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.digimon.domain.DigimonInfos;
import com.dro.modules.digimon.infra.DigimonInfosRepository;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.equipment.api.dto.response.DigimonEquipmentResponse;
import com.dro.modules.equipment.api.dto.response.EquipmentResponse;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.incubation.api.dto.response.IncubationResponse;
import com.dro.modules.incubation.api.dto.response.IncubationSlotResponse;
import com.dro.modules.incubation.api.dto.response.IncubationSlotsResponse;
import com.dro.modules.incubation.domain.Incubation;
import com.dro.modules.incubation.domain.IncubationStatus;
import com.dro.modules.incubation.domain.IncubatorRules;
import com.dro.modules.incubation.infra.IncubationRepository;
import com.dro.modules.inventory.domain.InventoryItem;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.mission.domain.MissionDefinitionEntity;
import com.dro.modules.mission.domain.MissionInstance;
import com.dro.modules.mission.infra.MissionDefinitionRepository;
import com.dro.modules.mission.domain.MissionStatus;
import com.dro.modules.mission.infra.MissionInstanceRepository;
import com.dro.modules.mission.infra.MissionTeamRepository;
import com.dro.modules.player.api.dto.response.ActiveMissionResponse;
import com.dro.modules.player.api.dto.response.InventorySummaryResponse;
import com.dro.modules.player.api.dto.response.PlayerDashboardResponse;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import com.dro.shared.exception.NotFoundException;
import com.dro.shared.util.TokenExtractor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Componente da camada de caso de uso da aplicação do módulo de Jogadores.
 */
@Service
public class GetPlayerDashboardUseCase {

    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final DigimonInfosRepository digimonInfosRepository;
    private final EquipmentRepository equipmentRepository;
    private final InventoryRepository inventoryRepository;
    private final MissionInstanceRepository missionInstanceRepository;
    private final IncubationRepository incubationRepository;
    private final MissionDefinitionRepository missionDefinitionRepository;
    private final MissionTeamRepository missionTeamRepository;
    private final ClanBonusService clanBonusService;

    public GetPlayerDashboardUseCase (PlayerRepository playerRepository, DigimonRepository digimonRepository, DigimonInfosRepository digimonInfosRepository, EquipmentRepository equipmentRepository, InventoryRepository inventoryRepository, MissionInstanceRepository missionInstanceRepository, IncubationRepository incubationRepository, MissionDefinitionRepository missionDefinitionRepository, MissionTeamRepository missionTeamRepository, ClanBonusService clanBonusService) {
        this.playerRepository = playerRepository;
        this.digimonRepository = digimonRepository;
        this.digimonInfosRepository = digimonInfosRepository;
        this.equipmentRepository = equipmentRepository;
        this.inventoryRepository = inventoryRepository;
        this.missionInstanceRepository = missionInstanceRepository;
        this.incubationRepository = incubationRepository;
        this.missionDefinitionRepository = missionDefinitionRepository;
        this.missionTeamRepository = missionTeamRepository;
        this.clanBonusService = clanBonusService;
    }

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

        IncubationSlotsResponse incubation = buildIncubation(playerId, player);

        long storedCount = digimonRepository.countByPlayerIdAndStatus(playerId, DigimonStatus.STORED);
        int activeCount = activeDigimon == null ? 0 : 1;
        var slotInfo = new PlayerDashboardResponse.SlotInfoResponse(
                activeCount, 1, (int) storedCount, player.getMaxStorageSlots()
        );

        return new PlayerDashboardResponse(
                player.getId(),
                player.getUsername(),
                player.getEmail(),
                player.getCreatedAt(),
                player.getUserType().name(),
                player.getDigitalData(),
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

        if (d == null || d.getStatus() != DigimonStatus.ACTIVE) {
            return null;
        }

        UUID clanId = player.getClanId();
        int maxEnergyBonus = clanId != null ? clanBonusService.getMaxEnergyBonus(clanId) : 0;

        d.regenerateEnergy(maxEnergyBonus);
        digimonRepository.save(d);

        List<Equipment> equipped = getEquippedItems(d);

        int equipBonusHp = EquipmentRules.totalBonusHp(equipped);
        int equipBonusAttack = EquipmentRules.totalBonusAttack(equipped);
        int equipBonusDefense = EquipmentRules.totalBonusDefense(equipped);

        double hpBonus = clanId != null ? clanBonusService.getHpBonusPercent(clanId) : 0.0;
        double atkBonus = clanId != null ? clanBonusService.getAttackBonusPercent(clanId) : 0.0;
        double defBonus = clanId != null ? clanBonusService.getDefenseBonusPercent(clanId) : 0.0;

        int clanBonusHp = calculateClanBonus(d.getHp() + equipBonusHp, hpBonus) - d.getHp() - equipBonusHp;
        int clanBonusAttack = calculateClanBonus(d.getAttack() + equipBonusAttack, atkBonus) - d.getAttack() - equipBonusAttack;
        int clanBonusDefense = calculateClanBonus(d.getDefense() + equipBonusDefense, defBonus) - d.getDefense() - equipBonusDefense;
        int clanBonusMaxEnergy = maxEnergyBonus;

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
                d.isLocked(),
                d.getDigimonInfoId(),
                info != null ? info.getAttribute().name() : null,
                info != null ? info.getElement().name() : null,
                info != null
                        ? info.getImageUrl()
                        : null,
                equipBonusHp,
                equipBonusAttack,
                equipBonusDefense,
                clanBonusHp,
                clanBonusAttack,
                clanBonusDefense,
                clanBonusMaxEnergy,
                d.isRarityChangedByDie(),
                d.getOriginalRarityBeforeDie(),
                d.getRarityChangedByDieAt()
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
                            remaining,
                            instance.isAutoRepeatEnabled(),
                            instance.isAutoClaimEnabled(),
                            instance.getTeamId(),
                            instance.getTeamId() == null
                                    ? null
                                    : missionTeamRepository.findById(instance.getTeamId())
                                            .map(team -> team.getName())
                                            .orElse("Time de missão"),
                            instance.getDigimonIds()
                    );
                })
                .toList();
    }

    private IncubationSlotsResponse buildIncubation(UUID playerId, Player player) {
        int unlockedSlots = Math.max(
                1,
                Math.min(IncubatorRules.TOTAL_SLOTS, player.getUnlockedIncubationSlots())
        );
        LocalDateTime now = LocalDateTime.now();
        Map<Integer, Incubation> activeBySlot = new HashMap<>();

        for (Incubation incubation : incubationRepository
                .findByPlayerIdAndStatusNotOrderBySlotNumberAsc(playerId, IncubationStatus.CLAIMED)) {
            if (incubation.getStatus() == IncubationStatus.IN_PROGRESS
                    && !incubation.getFinishAt().isAfter(now)) {
                incubation.markReadyIfFinished();
                incubationRepository.save(incubation);
            }
            activeBySlot.put(incubation.getSlotNumber(), incubation);
        }

        List<IncubationSlotResponse> slots = java.util.stream.IntStream
                .rangeClosed(1, IncubatorRules.TOTAL_SLOTS)
                .mapToObj(slotNumber -> new IncubationSlotResponse(
                        slotNumber,
                        slotNumber <= unlockedSlots,
                        toIncubationResponse(activeBySlot.get(slotNumber), now)
                ))
                .toList();

        return new IncubationSlotsResponse(
                IncubatorRules.TOTAL_SLOTS,
                unlockedSlots,
                slots
        );
    }

    private IncubationResponse toIncubationResponse(Incubation incubation, LocalDateTime now) {
        if (incubation == null) {
            return null;
        }
        long remaining = Math.max(0, Duration.between(now, incubation.getFinishAt()).getSeconds());
        return new IncubationResponse(
                incubation.getId(),
                incubation.getSlotNumber(),
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

    private int calculateClanBonus(int base, double percent) {
        if (percent <= 0) return base;
        return (int) Math.floor(base * (1.0 + percent));
    }
}
