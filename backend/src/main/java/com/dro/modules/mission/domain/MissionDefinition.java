package com.dro.modules.mission.domain;

import com.dro.modules.digimon.domain.enums.Stage;
import com.dro.modules.loot.domain.LootTable;
import java.util.List;

/**
 * Definição de uma missão disponível para o jogador.
 *
 * <p>O loot legado continua exposto apenas para compatibilidade com dados
 * antigos. Missões migradas para o novo sistema usam {@code chestCode} para
 * entregar um Baú da Área.</p>
 */
public class MissionDefinition {
    private final String id;
    private final String name;
    private final String description;
    private final Area area;
    private final Stage requiredStage;
    private final int requiredLevel;
    private final int baseXp;
    private final int baseBits;
    private final int energyCost;
    private final int durationSeconds;
    private final List<MissionReward> fixedRewards;
    private final LootTable lootTable;
    private final String chestCode;

    public MissionDefinition(final String id, final String name, final String description, final Area area, final Stage requiredStage, final int requiredLevel, final int baseXp, final int baseBits, final int energyCost, final int durationSeconds, final List<MissionReward> fixedRewards, final LootTable lootTable, final String chestCode) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.area = area;
        this.requiredStage = requiredStage;
        this.requiredLevel = requiredLevel;
        this.baseXp = baseXp;
        this.baseBits = baseBits;
        this.energyCost = energyCost;
        this.durationSeconds = durationSeconds;
        this.fixedRewards = fixedRewards;
        this.lootTable = lootTable;
        this.chestCode = chestCode;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Area getArea() {
        return this.area;
    }

    public Stage getRequiredStage() {
        return this.requiredStage;
    }

    public int getRequiredLevel() {
        return this.requiredLevel;
    }

    public int getBaseXp() {
        return this.baseXp;
    }

    public int getBaseBits() {
        return this.baseBits;
    }

    public int getEnergyCost() {
        return this.energyCost;
    }

    public int getDurationSeconds() {
        return this.durationSeconds;
    }

    public List<MissionReward> getFixedRewards() {
        return this.fixedRewards;
    }

    public LootTable getLootTable() {
        return this.lootTable;
    }

    public String getChestCode() {
        return this.chestCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof MissionDefinition)) return false;
        final MissionDefinition other = (MissionDefinition) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getRequiredLevel() != other.getRequiredLevel()) return false;
        if (this.getBaseXp() != other.getBaseXp()) return false;
        if (this.getBaseBits() != other.getBaseBits()) return false;
        if (this.getEnergyCost() != other.getEnergyCost()) return false;
        if (this.getDurationSeconds() != other.getDurationSeconds()) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$description = this.getDescription();
        final Object other$description = other.getDescription();
        if (this$description == null ? other$description != null : !this$description.equals(other$description)) return false;
        final Object this$area = this.getArea();
        final Object other$area = other.getArea();
        if (this$area == null ? other$area != null : !this$area.equals(other$area)) return false;
        final Object this$requiredStage = this.getRequiredStage();
        final Object other$requiredStage = other.getRequiredStage();
        if (this$requiredStage == null ? other$requiredStage != null : !this$requiredStage.equals(other$requiredStage)) return false;
        final Object this$fixedRewards = this.getFixedRewards();
        final Object other$fixedRewards = other.getFixedRewards();
        if (this$fixedRewards == null ? other$fixedRewards != null : !this$fixedRewards.equals(other$fixedRewards)) return false;
        final Object this$lootTable = this.getLootTable();
        final Object other$lootTable = other.getLootTable();
        if (this$lootTable == null ? other$lootTable != null : !this$lootTable.equals(other$lootTable)) return false;
        final Object this$chestCode = this.getChestCode();
        final Object other$chestCode = other.getChestCode();
        if (this$chestCode == null ? other$chestCode != null : !this$chestCode.equals(other$chestCode)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof MissionDefinition;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getRequiredLevel();
        result = result * PRIME + this.getBaseXp();
        result = result * PRIME + this.getBaseBits();
        result = result * PRIME + this.getEnergyCost();
        result = result * PRIME + this.getDurationSeconds();
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $description = this.getDescription();
        result = result * PRIME + ($description == null ? 43 : $description.hashCode());
        final Object $area = this.getArea();
        result = result * PRIME + ($area == null ? 43 : $area.hashCode());
        final Object $requiredStage = this.getRequiredStage();
        result = result * PRIME + ($requiredStage == null ? 43 : $requiredStage.hashCode());
        final Object $fixedRewards = this.getFixedRewards();
        result = result * PRIME + ($fixedRewards == null ? 43 : $fixedRewards.hashCode());
        final Object $lootTable = this.getLootTable();
        result = result * PRIME + ($lootTable == null ? 43 : $lootTable.hashCode());
        final Object $chestCode = this.getChestCode();
        result = result * PRIME + ($chestCode == null ? 43 : $chestCode.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "MissionDefinition(id=" + this.getId() + ", name=" + this.getName() + ", description=" + this.getDescription() + ", area=" + this.getArea() + ", requiredStage=" + this.getRequiredStage() + ", requiredLevel=" + this.getRequiredLevel() + ", baseXp=" + this.getBaseXp() + ", baseBits=" + this.getBaseBits() + ", energyCost=" + this.getEnergyCost() + ", durationSeconds=" + this.getDurationSeconds() + ", fixedRewards=" + this.getFixedRewards() + ", lootTable=" + this.getLootTable() + ", chestCode=" + this.getChestCode() + ")";
    }
}
