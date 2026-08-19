package com.dro.modules.event.application;

import com.dro.modules.inventory.domain.ItemType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class EventRewardMessageText {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int MAX_BODY_LENGTH = 1000;

    private EventRewardMessageText() {
    }

    public static String pendingBody(
            String customBody,
            int bitsAmount,
            String itemType,
            int itemQuantity,
            LocalDateTime expiresAt
    ) {
        String summary = "\n\nPrêmio disponível para resgate:\n"
                + "- Bits disponíveis: " + formatBits(bitsAmount) + "\n"
                + "- Item: " + formatItem(itemType, itemQuantity) + "\n"
                + "- Validade: até " + DATE_FORMAT.format(expiresAt) + "\n\n"
                + "Para receber a premiação, abra esta mensagem com um Digimon ativo e clique em “Resgatar prêmio”.";
        return fitWithSuffix(customBody.trim(), summary);
    }

    public static String claimedBody(
            String currentBody,
            int bitsAmount,
            String itemType,
            int itemQuantity,
            String digimonName,
            LocalDateTime claimedAt
    ) {
        String summary = "\n\nResgate concluído:\n"
                + "- Bits entregues: " + formatBits(bitsAmount) + "\n"
                + "- Item entregue: " + formatItem(itemType, itemQuantity) + "\n"
                + "- Destino: Digimon “" + digimonName + "”\n"
                + "- Resgatado em: " + DATE_FORMAT.format(claimedAt);
        String baseBody = currentBody.trim();
        int pendingSummaryIndex = baseBody.indexOf("\n\nPrêmio disponível para resgate:");
        if (pendingSummaryIndex >= 0) {
            baseBody = baseBody.substring(0, pendingSummaryIndex).trim();
        }
        return fitWithSuffix(baseBody, summary);
    }

    public static String formatItem(String itemType, int itemQuantity) {
        if (itemType == null || itemQuantity <= 0) {
            return "Nenhum item";
        }
        return itemQuantity + " × " + itemLabel(itemType);
    }

    private static String formatBits(int bitsAmount) {
        return String.format("%,d", bitsAmount).replace(',', '.') + " Bits";
    }

    private static String itemLabel(String itemType) {
        try {
            return switch (ItemType.valueOf(itemType)) {
                case POTION_SMALL -> "Poção pequena";
                case TRAINING_STONE -> "Pedra de treinamento";
                case DATA_CORE -> "Núcleo de dados";
                case DIGITAMA_STARTER -> "Digitama inicial";
                case DIGITAMA_FIRE -> "Digitama de fogo";
                case DIGITAMA_WATER -> "Digitama de água";
                case DIGITAMA_NATURE -> "Digitama da natureza";
                case INCUBATOR_COMMON -> "Incubadora comum";
                case INCUBATOR_RARE -> "Incubadora rara";
                case INCUBATOR_EPIC -> "Incubadora épica";
                case FRAGMENT_ROOKIE -> "Fragmento Rookie";
                case FRAGMENT_CHAMPION -> "Fragmento Champion";
                case FRAGMENT_ULTIMATE -> "Fragmento Ultimate";
                case FRAGMENT_MEGA -> "Fragmento Mega";
                case EVOLUTION_MATERIAL -> "Material de evolução";
                case REFINEMENT_STONE -> "Pedra de refinamento";
            };
        } catch (IllegalArgumentException exception) {
            return itemType;
        }
    }

    private static String fitWithSuffix(String prefix, String suffix) {
        int availablePrefixLength = MAX_BODY_LENGTH - suffix.length();
        if (availablePrefixLength <= 0) {
            return suffix.substring(0, MAX_BODY_LENGTH);
        }
        if (prefix.length() <= availablePrefixLength) {
            return prefix + suffix;
        }
        return prefix.substring(0, Math.max(0, availablePrefixLength - 1)) + "…" + suffix;
    }
}
