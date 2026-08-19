package com.dro.modules.digitama.domain.enums;

/**
 * Enumera estados, tipos ou classificações usados pelo módulo de Digitama.
 */
public enum DigitamaType {

    STARTER("DIGITAMA_STARTER"),
    FIRE("DIGITAMA_FIRE"),
    WATER("DIGITAMA_WATER"),
    NATURE("DIGITAMA_NATURE");

    private final String poolCode;

    DigitamaType(String poolCode) {
        this.poolCode = poolCode;
    }

    public String getPoolCode() {
        return poolCode;
    }
}