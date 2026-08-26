package com.dro.modules.event.application;

import com.dro.modules.inventory.domain.ItemType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Formata o conteúdo legível das mensagens de premiação no Correio.
 *
 * <p>O texto personalizado é preservado e recebe um resumo automático antes
 * do resgate. Depois da entrega, o resumo pendente é substituído por um
 * registro com o conteúdo entregue, o Digimon de destino e o horário.</p>
 */
public final class EventRewardMessageText {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int MAX_BODY_LENGTH = 1000;

    private EventRewardMessageText() {
    }

    /**
     * Acrescenta ao texto personalizado o resumo da premiação disponível.
     *
     * <p>O resultado permanece dentro do limite de corpo do Correio e informa
     * Bits, item, validade e a instrução de resgate.</p>
     */
    public static String pendingBody(
            String customBody,
            int bitsAmount,
            String itemType,
            int itemQuantity,
            LocalDateTime expiresAt
    ) {
        return pendingBody(customBody, bitsAmount, itemType, null, itemQuantity, expiresAt);
    }

    /** Acrescenta o nome exato da definição do catálogo quando disponível. */
    public static String pendingBody(
            String customBody,
            int bitsAmount,
            String itemType,
            String itemLabel,
            int itemQuantity,
            LocalDateTime expiresAt
    ) {
        String summary = "\n\nPrêmio disponível para resgate:\n"
                + "- Bits disponíveis: " + formatBits(bitsAmount) + "\n"
                + "- Item: " + formatItem(itemType, itemLabel, itemQuantity) + "\n"
                + "- Validade: até " + DATE_FORMAT.format(expiresAt) + "\n\n"
                + "Para receber a premiação, abra esta mensagem com um Digimon ativo e clique em “Resgatar prêmio”.";
        return fitWithSuffix(customBody.trim(), summary);
    }

    /**
     * Substitui o resumo pendente por um registro de entrega concluída.
     */
    public static String claimedBody(
            String currentBody,
            int bitsAmount,
            String itemType,
            int itemQuantity,
            String digimonName,
            LocalDateTime claimedAt
    ) {
        return claimedBody(currentBody, bitsAmount, itemType, null, itemQuantity, digimonName, claimedAt);
    }

    /** Registra a entrega usando o nome exato da definição do catálogo quando disponível. */
    public static String claimedBody(
            String currentBody,
            int bitsAmount,
            String itemType,
            String itemLabel,
            int itemQuantity,
            String digimonName,
            LocalDateTime claimedAt
    ) {
        String summary = "\n\nResgate concluído:\n"
                + "- Bits entregues: " + formatBits(bitsAmount) + "\n"
                + "- Item entregue: " + formatItem(itemType, itemLabel, itemQuantity) + "\n"
                + "- Destino: Digimon “" + digimonName + "”\n"
                + "- Resgatado em: " + DATE_FORMAT.format(claimedAt);
        String baseBody = currentBody.trim();
        int pendingSummaryIndex = baseBody.indexOf("\n\nPrêmio disponível para resgate:");
        if (pendingSummaryIndex >= 0) {
            baseBody = baseBody.substring(0, pendingSummaryIndex).trim();
        }
        return fitWithSuffix(baseBody, summary);
    }

    /**
     * Converte o tipo e a quantidade do item para uma descrição exibível ao jogador.
     */
    public static String formatItem(String itemType, int itemQuantity) {
        return formatItem(itemType, null, itemQuantity);
    }

    private static String formatItem(String itemType, String itemLabel, int itemQuantity) {
        if ((itemType == null && (itemLabel == null || itemLabel.isBlank())) || itemQuantity <= 0) {
            return "Nenhum item";
        }
        String label = itemLabel == null || itemLabel.isBlank() ? itemLabel(itemType) : itemLabel;
        return itemQuantity + " × " + label;
    }

    private static String formatBits(int bitsAmount) {
        return String.format("%,d", bitsAmount).replace(',', '.') + " Bits";
    }

    /** Converte o valor persistido do item para o rótulo em Português exibido ao jogador. */
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
                case INCUBATION_SLOT_UNLOCK -> "Expansor de slot de incubação";
                case FRAGMENT_ROOKIE -> "Fragmento Rookie";
                case FRAGMENT_CHAMPION -> "Fragmento Champion";
                case FRAGMENT_ULTIMATE -> "Fragmento Ultimate";
                case FRAGMENT_MEGA -> "Fragmento Mega";
                case EVOLUTION_MATERIAL -> "Material de evolução";
                case LOOT_CHEST -> "Baú temático";
                case REFINEMENT_STONE -> "Pedra de refinamento";
            };
        } catch (IllegalArgumentException exception) {
            return itemType;
        }
    }

    /** Preserva o sufixo automático mesmo quando o texto personalizado excede 1.000 caracteres. */
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
