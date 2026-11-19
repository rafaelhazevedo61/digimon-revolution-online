package com.dro.modules.auction.domain;

import com.dro.modules.equipment.domain.Equipment;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Immutable sale-time details; independent of the live equipment's later upgrades or deletion. */
@Embeddable
public class AuctionEquipmentSnapshot {
    @Column(name = "equipment_name")
    private String name;
    @Column(name = "equipment_slot")
    private String slot;
    @Column(name = "equipment_rarity")
    private String rarity;
    @Column(name = "equipment_set_code")
    private String setCode;
    @Column(name = "equipment_tier")
    private int tier;
    @Column(name = "equipment_refinement_level")
    private int refinementLevel;
    @Column(name = "equipment_ascension_level")
    private int ascensionLevel;
    @Column(name = "equipment_bonus_hp")
    private int bonusHp;
    @Column(name = "equipment_bonus_attack")
    private int bonusAttack;
    @Column(name = "equipment_bonus_defense")
    private int bonusDefense;
    @Column(name = "equipment_effective_bonus_hp")
    private int effectiveBonusHp;
    @Column(name = "equipment_effective_bonus_attack")
    private int effectiveBonusAttack;
    @Column(name = "equipment_effective_bonus_defense")
    private int effectiveBonusDefense;
    protected AuctionEquipmentSnapshot() { }

    public static AuctionEquipmentSnapshot from(Equipment equipment) {
        AuctionEquipmentSnapshot snapshot = new AuctionEquipmentSnapshot();
        snapshot.name = equipment.getName();
        snapshot.slot = equipment.getSlot().name();
        snapshot.rarity = equipment.getRarity().name();
        snapshot.setCode = equipment.getSetCode();
        snapshot.tier = equipment.getTier();
        snapshot.refinementLevel = equipment.getRefinementLevel();
        snapshot.ascensionLevel = equipment.getAscensionLevel();
        snapshot.bonusHp = equipment.getBonusHp();
        snapshot.bonusAttack = equipment.getBonusAttack();
        snapshot.bonusDefense = equipment.getBonusDefense();
        snapshot.effectiveBonusHp = equipment.getEffectiveBonusHp();
        snapshot.effectiveBonusAttack = equipment.getEffectiveBonusAttack();
        snapshot.effectiveBonusDefense = equipment.getEffectiveBonusDefense();
        return snapshot;
    }

    public String getName() { return name; }
    public String getSlot() { return slot; }
    public String getRarity() { return rarity; }
    public String getSetCode() { return setCode; }
    public int getTier() { return tier; }
    public int getRefinementLevel() { return refinementLevel; }
    public int getAscensionLevel() { return ascensionLevel; }
    public int getBonusHp() { return bonusHp; }
    public int getBonusAttack() { return bonusAttack; }
    public int getBonusDefense() { return bonusDefense; }
    public int getEffectiveBonusHp() { return effectiveBonusHp; }
    public int getEffectiveBonusAttack() { return effectiveBonusAttack; }
    public int getEffectiveBonusDefense() { return effectiveBonusDefense; }
}
