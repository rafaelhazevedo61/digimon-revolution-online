package com.dro.modules.arena.application;

import com.dro.modules.digimon.domain.Digimon;
import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.infra.EquipmentRepository;
import com.dro.modules.clan.application.ClanBonusService;
import com.dro.modules.player.infra.PlayerRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DigimonPowerServiceTest {
    @Test void towerAndArenaShareEquippedStatsAndClanBonuses() {
        var equipment = mock(EquipmentRepository.class);
        var clan = mock(ClanBonusService.class);
        var players = mock(PlayerRepository.class);
        var service = new DigimonPowerService(equipment, clan, players);
        var digimon = mock(Digimon.class);
        UUID id = UUID.randomUUID(), clanId = UUID.randomUUID();
        when(digimon.getId()).thenReturn(id);
        when(digimon.getHp()).thenReturn(100);
        when(digimon.getAttack()).thenReturn(20);
        when(digimon.getDefense()).thenReturn(10);
        var worn = mock(Equipment.class); var unused = mock(Equipment.class);
        when(worn.isEquipped()).thenReturn(true);
        when(worn.getEffectiveBonusHp()).thenReturn(20);
        when(worn.getEffectiveBonusAttack()).thenReturn(10);
        when(worn.getEffectiveBonusDefense()).thenReturn(10);
        when(equipment.findByDigimonId(id)).thenReturn(List.of(worn, unused));
        when(clan.getHpBonusPercent(clanId)).thenReturn(.25);
        when(clan.getAttackBonusPercent(clanId)).thenReturn(.2);
        when(clan.getDefenseBonusPercent(clanId)).thenReturn(.5);
        assertEquals(new DigimonPowerService.CombatStats(150,36,30), service.effectiveStats(digimon, clanId));
        assertEquals(150*.3 + 36*1.5 + 30, service.calculatePower(digimon, clanId));
        verify(unused, never()).getEffectiveBonusHp();
    }
}
