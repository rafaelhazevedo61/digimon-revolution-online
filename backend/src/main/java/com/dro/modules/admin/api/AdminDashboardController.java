package com.dro.modules.admin.api;

import com.dro.modules.activitycalendar.infra.ActivityMonthlyAggregateProjection;
import com.dro.modules.activitycalendar.infra.ActivityPointEventRepository;
import com.dro.modules.admin.api.dto.response.AdminActivityMonthlyResponse;
import com.dro.modules.admin.api.dto.response.AdminDashboardSummaryResponse;
import com.dro.modules.digimon.domain.enums.DigimonStatus;
import com.dro.modules.digimon.infra.DigimonRepository;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.equipment.infra.EquipmentTemplateRepository;
import com.dro.modules.inventory.infra.InventoryRepository;
import com.dro.modules.inventory.infra.ItemDefinitionRepository;
import com.dro.modules.loot.infra.LootTableRepository;
import com.dro.modules.player.infra.PlayerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {
    private final PlayerRepository playerRepository;
    private final DigimonRepository digimonRepository;
    private final InventoryRepository inventoryRepository;
    private final EquipmentRepository equipmentRepository;
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final EquipmentTemplateRepository equipmentTemplateRepository;
    private final LootTableRepository lootTableRepository;
    private final ActivityPointEventRepository activityPointEventRepository;

    @GetMapping("/summary")
    public ResponseEntity<AdminDashboardSummaryResponse> summary() {
        long playersTotal = playerRepository.count();
        long playersWithoutActiveDigimon = playerRepository.countByActiveDigimonIdIsNull();
        long digimonsTotal = digimonRepository.count();
        long activeDigimons = digimonRepository.countByStatus(DigimonStatus.ACTIVE);
        long orphanDigimons = digimonRepository.countByPlayerIdIsNull();
        long inventoryStacks = inventoryRepository.count();
        long inventoryQuantities = inventoryRepository.sumQuantities();
        long orphanInventory = inventoryRepository.countByPlayerIdIsNull();
        long equipmentTotal = equipmentRepository.count();
        long equipped = equipmentRepository.countByEquippedTrue();
        long orphanEquipment = equipmentRepository.countByPlayerIdIsNull();
        long itemDefinitions = itemDefinitionRepository.count();
        long equipmentTemplates = equipmentTemplateRepository.count();
        long lootTables = lootTableRepository.count();
        long activeLootTables = lootTableRepository.findByActiveTrueOrderByNameAsc().size();

        List<AdminDashboardSummaryResponse.Alert> alerts = new ArrayList<>();
        addAlert(alerts, "WARNING", "PLAYERS_WITHOUT_ACTIVE_DIGIMON", "Jogadores sem Digimon ativo", "Existem jogadores sem um Digimon ativo selecionado.", playersWithoutActiveDigimon);
        addAlert(alerts, "ERROR", "ORPHAN_DIGIMONS", "Digimons sem proprietário", "Existem registros de Digimon sem player_id.", orphanDigimons);
        addAlert(alerts, "ERROR", "ORPHAN_INVENTORY", "Itens de inventário sem proprietário", "Existem registros de inventory_items sem player_id.", orphanInventory);
        addAlert(alerts, "ERROR", "ORPHAN_EQUIPMENT", "Equipamentos sem proprietário", "Existem registros de inventory_equipments sem player_id.", orphanEquipment);

        var response = new AdminDashboardSummaryResponse(
                new AdminDashboardSummaryResponse.Metrics(playersTotal, playersWithoutActiveDigimon, playersWithoutActiveDigimon),
                new AdminDashboardSummaryResponse.Metrics(digimonsTotal, activeDigimons, orphanDigimons),
                new AdminDashboardSummaryResponse.Metrics(inventoryStacks, inventoryQuantities, orphanInventory),
                new AdminDashboardSummaryResponse.Metrics(equipmentTotal, equipped, orphanEquipment),
                new AdminDashboardSummaryResponse.Metrics(itemDefinitions, 0, 0),
                new AdminDashboardSummaryResponse.Metrics(equipmentTemplates, 0, 0),
                new AdminDashboardSummaryResponse.Metrics(lootTables, activeLootTables, 0),
                alerts,
                new AdminDashboardSummaryResponse.SystemStatus("UP", LocalDateTime.now())
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activity/monthly")
    public ResponseEntity<AdminActivityMonthlyResponse> activityMonthly() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(11);
        List<AdminActivityMonthlyResponse.Month> months = activityPointEventRepository
                .aggregateMonthlySince(firstMonth.atDay(1))
                .stream()
                .map(this::toActivityMonth)
                .toList();
        return ResponseEntity.ok(new AdminActivityMonthlyResponse(
                firstMonth.toString(), currentMonth.toString(), months));
    }

    private AdminActivityMonthlyResponse.Month toActivityMonth(ActivityMonthlyAggregateProjection row) {
        return new AdminActivityMonthlyResponse.Month(
                row.getYearMonth(),
                valueOrZero(row.getPoints()),
                valueOrZero(row.getActivePlayers()),
                valueOrZero(row.getEvents()));
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    private void addAlert(List<AdminDashboardSummaryResponse.Alert> alerts, String severity, String code, String title, String message, long count) {
        if (count > 0) alerts.add(new AdminDashboardSummaryResponse.Alert(severity, code, title, message, count));
    }

    public AdminDashboardController(
            PlayerRepository playerRepository,
            DigimonRepository digimonRepository,
            InventoryRepository inventoryRepository,
            EquipmentRepository equipmentRepository,
            ItemDefinitionRepository itemDefinitionRepository,
            EquipmentTemplateRepository equipmentTemplateRepository,
            LootTableRepository lootTableRepository,
            ActivityPointEventRepository activityPointEventRepository
    ) {
        this.playerRepository = playerRepository;
        this.digimonRepository = digimonRepository;
        this.inventoryRepository = inventoryRepository;
        this.equipmentRepository = equipmentRepository;
        this.itemDefinitionRepository = itemDefinitionRepository;
        this.equipmentTemplateRepository = equipmentTemplateRepository;
        this.lootTableRepository = lootTableRepository;
        this.activityPointEventRepository = activityPointEventRepository;
    }
}
