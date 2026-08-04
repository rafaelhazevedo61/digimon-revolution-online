package com.dro.modules.arena.application;

import com.dro.modules.boss.domain.BossCombatRules;
import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRules;
import com.dro.modules.equipment.infra.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Calcula o poder efetivo de um Digimon (stats base + bônus de equipamentos),
 * usando a mesma fórmula de poder empregada no combate contra bosses.
 */
@Service
@RequiredArgsConstructor
public class DigimonPowerService {

    private final EquipmentRepository equipmentRepository;

    public double calculatePower(Digimon digimon) {
        List<Equipment> equipped = equipmentRepository.findByDigimonId(digimon.getId())
                .stream().filter(Equipment::isEquipped).toList();

        int totalHp = digimon.getHp() + EquipmentRules.totalBonusHp(equipped);
        int totalAtk = digimon.getAttack() + EquipmentRules.totalBonusAttack(equipped);
        int totalDef = digimon.getDefense() + EquipmentRules.totalBonusDefense(equipped);

        return BossCombatRules.calculatePower(totalHp, totalAtk, totalDef);
    }
}
