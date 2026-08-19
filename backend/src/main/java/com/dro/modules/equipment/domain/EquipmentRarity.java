package com.dro.modules.equipment.domain;

/**
 * Enumera estados, tipos ou classificações usados pelo módulo de Equipamentos.
 */
public enum EquipmentRarity {
    COMMON(1.00),
    RARE(1.15),
    EPIC(1.30),
    LEGENDARY(1.50);

    private final double statMultiplier;

    EquipmentRarity(double statMultiplier) {
        this.statMultiplier = statMultiplier;
    }

    public double getStatMultiplier() {
        return statMultiplier;
    }
}
