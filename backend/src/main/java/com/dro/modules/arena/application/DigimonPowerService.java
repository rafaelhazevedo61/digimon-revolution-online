package com.dro.modules.arena.application;

import com.dro.modules.boss.domain.BossCombatRules;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.player.domain.Player;
import com.dro.modules.player.infra.PlayerRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

/**
 * Componente da camada de serviço de aplicação do módulo de Arena.
 */
@Service
public class DigimonPowerService {
    private final EquipmentRepository equipmentRepository;
    private final ClanBonusService clanBonusService;
    private final PlayerRepository playerRepository;

    public double calculatePower(Digimon digimon) {
        UUID clanId = playerRepository.findById(digimon.getPlayerId()).map(Player::getClanId).orElse(null);
        return calculatePower(digimon, clanId);
    }

    public double calculatePower(Digimon digimon, UUID clanId) {
        List<Equipment> equipped = equipmentRepository.findByDigimonId(digimon.getId()).stream().filter(Equipment::isEquipped).toList();
        double atkBonus = clanId != null ? clanBonusService.getAttackBonusPercent(clanId) : 0.0;
        double defBonus = clanId != null ? clanBonusService.getDefenseBonusPercent(clanId) : 0.0;
        double hpBonus = clanId != null ? clanBonusService.getHpBonusPercent(clanId) : 0.0;
        int totalHp = applyBonus(digimon.getHp() + EquipmentRules.totalBonusHp(equipped), hpBonus);
        int totalAtk = applyBonus(digimon.getAttack() + EquipmentRules.totalBonusAttack(equipped), atkBonus);
        int totalDef = applyBonus(digimon.getDefense() + EquipmentRules.totalBonusDefense(equipped), defBonus);
        return BossCombatRules.calculatePower(totalHp, totalAtk, totalDef);
    }

    private int applyBonus(int base, double percent) {
        if (percent <= 0) return base;
        return (int) Math.floor(base * (1.0 + percent));
    }

    public DigimonPowerService(final EquipmentRepository equipmentRepository, final ClanBonusService clanBonusService, final PlayerRepository playerRepository) {
        this.equipmentRepository = equipmentRepository;
        this.clanBonusService = clanBonusService;
        this.playerRepository = playerRepository;
    }
}
