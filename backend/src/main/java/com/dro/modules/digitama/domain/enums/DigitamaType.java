package com.dro.modules.digitama.domain.enums;

import com.dro.modules.digimon.domain.enums.Element;

/**
 * Enumera estados, tipos ou classificações usados pelo módulo de Digitama.
 */
public enum DigitamaType {

    STARTER("DIGITAMA_STARTER", null),
    FIRE("DIGITAMA_FIRE", Element.FIRE),
    WATER("DIGITAMA_WATER", Element.WATER),
    NATURE("DIGITAMA_NATURE", Element.WOOD),
    EARTH("DIGITAMA_EARTH", Element.EARTH),
    WIND("DIGITAMA_WIND", Element.WIND),
    LIGHT("DIGITAMA_LIGHT", Element.LIGHT),
    DARK("DIGITAMA_DARK", Element.DARK),
    THUNDER("DIGITAMA_THUNDER", Element.THUNDER),
    NEUTRAL("DIGITAMA_NEUTRAL", Element.NEUTRAL),
    ICE("DIGITAMA_ICE", Element.ICE),
    STEEL("DIGITAMA_STEEL", Element.STEEL);

    private final String poolCode;
    private final Element element;

    DigitamaType(String poolCode, Element element) {
        this.poolCode = poolCode;
        this.element = element;
    }

    public String getPoolCode() {
        return poolCode;
    }

    public Element getElement() {
        return element;
    }

    public boolean accepts(Element candidateElement) {
        return element == null || element == candidateElement;
    }

    public static DigitamaType fromPoolCode(String poolCode) {
        for (DigitamaType type : values()) {
            if (type.poolCode.equals(poolCode)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown digitama pool: " + poolCode);
    }
}