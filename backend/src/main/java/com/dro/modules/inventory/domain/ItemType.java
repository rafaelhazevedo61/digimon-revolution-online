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

    // Digitamas
    DIGITAMA_STARTER,
    DIGITAMA_FIRE,
    DIGITAMA_WATER,
    DIGITAMA_NATURE,

    // Incubadoras
    INCUBATOR_COMMON,
    INCUBATOR_RARE,
    INCUBATOR_EPIC,

    // Expansão da incubadora
    INCUBATION_SLOT_UNLOCK,

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
    REFINEMENT_STONE

}
