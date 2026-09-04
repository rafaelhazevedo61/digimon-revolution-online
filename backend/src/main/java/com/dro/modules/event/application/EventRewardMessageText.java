package com.dro.modules.event.application;

import com.dro.modules.inventory.domain.ItemType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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

    /** Item apresentado no resumo automático da premiação. */
    public record ItemLabel(String itemType, String label, int quantity) {
    }

    /** Acrescenta ao texto personalizado o resumo de uma premiação legada de item único. */
    public static String pendingBody(
            String customBody,
            int bitsAmount,
            String itemType,
            int itemQuantity,
            LocalDateTime expiresAt
    ) {
        return pendingBody(customBody, bitsAmount, itemType == null ? List.of() : List.of(new ItemLabel(itemType, null, itemQuantity)), expiresAt);
    }

    /** Acrescenta ao texto personalizado o resumo da premiação com todos os itens. */
    public static String pendingBody(
            String customBody,
            int bitsAmount,
            List<ItemLabel> items,
            LocalDateTime expiresAt
    ) {
        String summary = "\n\nPrêmio disponível para resgate:\n"
                + "- Bits disponíveis: " + formatBits(bitsAmount) + "\n"
                + "- Itens disponíveis: " + formatItems(items) + "\n"
                + "- Validade: até " + DATE_FORMAT.format(expiresAt) + "\n\n"
                + "Para receber a premiação, abra esta mensagem com um Digimon ativo e clique em “Resgatar prêmio”.";
        return fitWithSuffix(customBody.trim(), summary);
    }

    /** Acrescenta o nome exato de uma definição para mensagens de item único. */
    public static String pendingBody(
            String customBody,
            int bitsAmount,
            String itemType,
            String itemLabel,
            int itemQuantity,
            LocalDateTime expiresAt
    ) {
        return pendingBody(customBody, bitsAmount, itemType == null ? List.of() : List.of(new ItemLabel(itemType, itemLabel, itemQuantity)), expiresAt);
    }

    /** Substitui o resumo pendente por um registro de entrega legada de item único. */
    public static String claimedBody(
            String currentBody,
            int bitsAmount,
            String itemType,
            int itemQuantity,
            String digimonName,
            LocalDateTime claimedAt
    ) {
        return claimedBody(currentBody, bitsAmount, itemType == null ? List.of() : List.of(new ItemLabel(itemType, null, itemQuantity)), digimonName, claimedAt);
    }

    /** Substitui o resumo pendente pelo registro completo da entrega. */
    public static String claimedBody(
            String currentBody,
            int bitsAmount,
            List<ItemLabel> items,
            String digimonName,
            LocalDateTime claimedAt
    ) {
        String summary = "\n\nResgate concluído:\n"
                + "- Bits entregues: " + formatBits(bitsAmount) + "\n"
                + "- Itens entregues: " + formatItems(items) + "\n"
                + "- Destino: Digimon “" + digimonName + "”\n"
                + "- Resgatado em: " + DATE_FORMAT.format(claimedAt);
        String baseBody = currentBody.trim();
        int pendingSummaryIndex = baseBody.indexOf("\n\nPrêmio disponível para resgate:");
        if (pendingSummaryIndex >= 0) {
            baseBody = baseBody.substring(0, pendingSummaryIndex).trim();
        }
        return fitWithSuffix(baseBody, summary);
    }

    /** Registra a entrega usando o nome exato de uma definição de item único. */
    public static String claimedBody(
            String currentBody,
            int bitsAmount,
            String itemType,
            String itemLabel,
            int itemQuantity,
            String digimonName,
            LocalDateTime claimedAt
    ) {
        return claimedBody(currentBody, bitsAmount, itemType == null ? List.of() : List.of(new ItemLabel(itemType, itemLabel, itemQuantity)), digimonName, claimedAt);
    }

    /** Converte um item legado em uma descrição exibível ao jogador. */
    public static String formatItem(String itemType, int itemQuantity) {
        return formatItems(itemType == null ? List.of() : List.of(new ItemLabel(itemType, null, itemQuantity)));
    }

    private static String formatItems(List<ItemLabel> items) {
        if (items == null || items.isEmpty()) {
            return "Nenhum item";
        }
        String formatted = items.stream()
                .filter(item -> item != null && item.quantity() > 0)
                .map(item -> item.quantity() + " × " + (item.label() == null || item.label().isBlank() ? itemLabel(item.itemType()) : item.label()))
                .collect(Collectors.joining(", "));
        return formatted.isBlank() ? "Nenhum item" : formatted;
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
                case CODE_INFINITE -> "Código Infinito";
                case DIGITAMA_STARTER -> "Digitama inicial";
                case DIGITAMA_FIRE -> "Digitama de fogo";
                case DIGITAMA_WATER -> "Digitama de água";
                case DIGITAMA_NATURE -> "Digitama de planta";
                case DIGITAMA_EARTH -> "Digitama de terra";
                case DIGITAMA_WIND -> "Digitama de vento";
                case DIGITAMA_LIGHT -> "Digitama de luz";
                case DIGITAMA_DARK -> "Digitama de trevas";
                case DIGITAMA_THUNDER -> "Digitama de trovão";
                case DIGITAMA_NEUTRAL -> "Digitama neutro";
                case DIGITAMA_ICE -> "Digitama de gelo";
                case DIGITAMA_STEEL -> "Digitama de metal";
                case INCUBATOR_COMMON -> "Incubadora comum";
                case INCUBATOR_RARE -> "Incubadora rara";
                case INCUBATOR_EPIC -> "Incubadora épica";
                case INCUBATOR_LEGENDARY -> "Incubadora lendária";
                case INCUBATION_SLOT_UNLOCK -> "Expansor de slot de incubação";
                case MISSION_SLOT_UNLOCK -> "Expansor de slot de missão";
                case STORAGE_SLOT_1 -> "+1 Storage";
                case STORAGE_SLOT_5 -> "+5 Storage";
                case STORAGE_SLOT_10 -> "+10 Storage";
                case XP_DISC_1 -> "Disco de XP +1%";
                case XP_DISC_3 -> "Disco de XP +3%";
                case XP_DISC_5 -> "Disco de XP +5%";
                case XP_DISC_10 -> "Disco de XP +10%";
                case XP_DISC_15 -> "Disco de XP +15%";
                case XP_DISC_20 -> "Disco de XP +20%";
                case FRAGMENT_ROOKIE -> "Fragmento Rookie";
                case FRAGMENT_CHAMPION -> "Fragmento Champion";
                case FRAGMENT_ULTIMATE -> "Fragmento Ultimate";
                case FRAGMENT_MEGA -> "Fragmento Mega";
                case EVOLUTION_MATERIAL -> "Material de evolução";
                case LOOT_CHEST -> "Baú temático";
                case REFINEMENT_STONE -> "Pedra de refinamento";
                case REFINEMENT_SUCCESS_BOOST -> "Pergaminho de Refinamento";
                case ASCENSION_CORE -> "Núcleo de Ascensão";
                case RARITY_REROLL -> "Dado de Raridade";
                case RARITY_PRESERVATION -> "Cristal de Preservação de Raridade";
                case COLLECTION_DIGIVICE -> "Digivice de Registro";
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
