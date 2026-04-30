package com.dro.modules.equipment.application;

import com.dro.modules.equipment.domain.Equipment;
import com.dro.modules.equipment.domain.EquipmentRarity;
import com.dro.modules.equipment.domain.EquipmentSlot;
import com.dro.modules.equipment.infra.EquipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrantEquipmentUseCaseTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private GrantEquipmentUseCase grantEquipmentUseCase;

    @Test
    void execute_createsEquipmentFromTemplate() {
        UUID digimonId = UUID.randomUUID();

        UUID result = grantEquipmentUseCase.execute(digimonId, "Iron Claw");

        assertNotNull(result);

        ArgumentCaptor<Equipment> captor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentRepository).save(captor.capture());

        Equipment saved = captor.getValue();
        assertEquals("Iron Claw", saved.getName());
        assertEquals(EquipmentSlot.WEAPON, saved.getSlot());
        assertEquals(EquipmentRarity.COMMON, saved.getRarity());
        assertEquals(0, saved.getBonusHp());
        assertEquals(5, saved.getBonusAttack());
        assertEquals(0, saved.getBonusDefense());
        assertEquals(digimonId, saved.getDigimonId());
        assertFalse(saved.isEquipped());
    }

    @Test
    void execute_throwsWhenTemplateNotFound() {
        assertThrows(RuntimeException.class,
                () -> grantEquipmentUseCase.execute(UUID.randomUUID(), "Nonexistent Sword"));
    }

    @Test
    void execute_grantsLegendaryEquipment() {
        UUID digimonId = UUID.randomUUID();

        grantEquipmentUseCase.execute(digimonId, "Omega Blade");

        ArgumentCaptor<Equipment> captor = ArgumentCaptor.forClass(Equipment.class);
        verify(equipmentRepository).save(captor.capture());

        Equipment saved = captor.getValue();
        assertEquals("Omega Blade", saved.getName());
        assertEquals(EquipmentRarity.LEGENDARY, saved.getRarity());
        assertEquals(5, saved.getBonusHp());
        assertEquals(40, saved.getBonusAttack());
        assertEquals(5, saved.getBonusDefense());
    }
}
