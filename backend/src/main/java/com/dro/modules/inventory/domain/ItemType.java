package com.dro.modules.inventory.domain;

/**
 * Enumera estados, tipos ou classificações usados pelo módulo de Inventário.
 */
public enum ItemType {

    // Pocoes
    POTION_SMALL,

    // Recursos
    TRAINING_STONE,
    DATA_CORE,
    CODE_INFINITE,

    // Digitamas
    DIGITAMA_STARTER,
    DIGITAMA_FIRE,
    DIGITAMA_WATER,
    DIGITAMA_NATURE,
    DIGITAMA_EARTH,
    DIGITAMA_WIND,
    DIGITAMA_LIGHT,
    DIGITAMA_DARK,
    DIGITAMA_THUNDER,
    DIGITAMA_NEUTRAL,
    DIGITAMA_ICE,
    DIGITAMA_STEEL,

    // Incubadoras
    INCUBATOR_COMMON,
    INCUBATOR_RARE,
    INCUBATOR_EPIC,

    // Expansão da incubadora
    INCUBATION_SLOT_UNLOCK,

    // Expansão permanente do Storage
    STORAGE_SLOT_1,
    STORAGE_SLOT_5,
    STORAGE_SLOT_10,

    // Discos de XP instantâneo
    XP_DISC_1,
    XP_DISC_3,
    XP_DISC_5,
    XP_DISC_10,
    XP_DISC_15,
    XP_DISC_20,

    // Fragmentos (legado — mantidos para compatibilidade)
    FRAGMENT_ROOKIE,
    FRAGMENT_CHAMPION,
    FRAGMENT_ULTIMATE,
    FRAGMENT_MEGA,

    // Materiais de evolução (diferenciados por material_code)
    EVOLUTION_MATERIAL,

    // Baús temáticos — diferenciados pelo código da definição do item
    LOOT_CHEST,

    // Material de refinamento
    REFINEMENT_STONE,

    // Reroll de raridade do Digimon
    RARITY_REROLL

}
