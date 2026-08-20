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
     *
     * @param customBody texto escrito pelo administrador
     * @param bitsAmount quantidade de Bits a entregar
     * @param itemType tipo do item ou {@code null} quando não houver item
     * @param itemQuantity quantidade do item
     * @param expiresAt instante limite para o resgate
     * @return corpo pronto para ser persistido na mensagem pendente
     */
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

    /**
     * Substitui o resumo pendente por um registro da entrega concluída.
     *
     * @param currentBody corpo atual da mensagem, possivelmente com resumo pendente
     * @param bitsAmount quantidade de Bits entregue
     * @param itemType tipo do item ou {@code null} quando não houver item
     * @param itemQuantity quantidade do item entregue
     * @param digimonName nome do Digimon que recebeu o prêmio
     * @param claimedAt instante em que a entrega foi concluída
     * @return corpo atualizado, limitado ao tamanho máximo do Correio
     */
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

    /**
     * Converte o tipo e a quantidade do item para uma descrição exibível ao jogador.
     *
     * @param itemType valor persistido do {@link ItemType}
     * @param itemQuantity quantidade de unidades
     * @return descrição traduzida do item ou {@code Nenhum item}
     */
    public static String formatItem(String itemType, int itemQuantity) {
        if (itemType == null || itemQuantity <= 0) {
            return "Nenhum item";
        }
        return itemQuantity + " × " + itemLabel(itemType);
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
