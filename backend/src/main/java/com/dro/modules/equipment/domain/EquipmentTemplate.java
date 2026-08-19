package com.dro.modules.equipment.domain;


/**
 * Componente da camada de modelo de domínio do módulo de Equipamentos.
 */
public class EquipmentTemplate {

    private final String name;
    private final EquipmentSlot slot;
    private final EquipmentRarity rarity;
    private final String setCode;
    private final int tier;
    private final int bonusHp;
    private final int bonusAttack;
    private final int bonusDefense;

    public EquipmentTemplate(String name, EquipmentSlot slot, EquipmentRarity rarity,
                             String setCode, int tier,
                             int bonusHp, int bonusAttack, int bonusDefense) {
        this.name = name;
        this.slot = slot;
        this.rarity = rarity;
        this.setCode = setCode;
        this.tier = tier;
        this.bonusHp = bonusHp;
        this.bonusAttack = bonusAttack;
        this.bonusDefense = bonusDefense;
    }

    public String getName() { return name; }
    public EquipmentSlot getSlot() { return slot; }
    public EquipmentRarity getRarity() { return rarity; }
    public String getSetCode() { return setCode; }
    public int getTier() { return tier; }
    public int getBonusHp() { return bonusHp; }
    public int getBonusAttack() { return bonusAttack; }
    public int getBonusDefense() { return bonusDefense; }
}
