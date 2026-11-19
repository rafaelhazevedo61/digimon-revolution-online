package com.dro.modules.auction.domain;

import com.dro.shared.exception.BadRequestException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Regras quantitativas e de validação da Casa de Leilões.
 *
 * <p>Os valores de comissão são armazenados em basis points: 500 representa
 * 5%, 750 representa 7,5% e 1.000 representa 10%. A taxa de publicação é
 * cobrada separadamente da comissão da venda.</p>
 */
public final class AuctionRules {
    /**
     * Taxa fixa cobrada no momento da publicação do anúncio.
     */
    public static final int LISTING_FEE = 100;
    /**
     * Quantidade máxima de anúncios ativos por jogador.
     */
    public static final int MAX_ACTIVE_LISTINGS_PER_PLAYER = 10;
    /**
     * Comissão do vendedor para anúncios de 24 horas, em basis points.
     */
    public static final int SELLER_FEE_RATE_24_HOURS_BPS = 500;
    /**
     * Comissão do vendedor para anúncios de 48 horas, em basis points.
     */
    public static final int SELLER_FEE_RATE_48_HOURS_BPS = 750;
    /**
     * Comissão do vendedor para anúncios de 72 horas, em basis points.
     */
    public static final int SELLER_FEE_RATE_72_HOURS_BPS = 1000;
    public static final Set<Integer> ALLOWED_DURATIONS_HOURS = Set.of(24, 48, 72);

    /**
     * Valida os campos básicos de um anúncio.
     *
     * @param quantity quantidade positiva de unidades
     * @param unitPrice preço positivo por unidade
     * @param durationHours duração permitida de 24, 48 ou 72 horas
     * @throws BadRequestException quando um valor não respeita as regras do marketplace
     */
    public static void validateListing(int quantity, int unitPrice, int durationHours) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }
        if (unitPrice <= 0) {
            throw new BadRequestException("Unit price must be greater than zero");
        }
        if (!ALLOWED_DURATIONS_HOURS.contains(durationHours)) {
            throw new BadRequestException("Duration must be 24, 48 or 72 hours");
        }
    }

    /**
     * Calcula o valor bruto de uma compra ou venda parcial.
     *
     * @param quantity quantidade de unidades
     * @param unitPrice preço de cada unidade
     * @return {@code quantity * unitPrice}
     * @throws BadRequestException quando o resultado não cabe em um inteiro
     */
    public static int calculateGrossAmount(int quantity, int unitPrice) {
        long gross = (long) quantity * unitPrice;
        if (gross > Integer.MAX_VALUE) {
            throw new BadRequestException("Total amount is too high");
        }
        return (int) gross;
    }

    /**
     * Calcula a comissão padrão de 24 horas.
     *
     * @param grossAmount valor bruto da venda
     * @return comissão em Bits, arredondada para baixo
     */
    public static int calculateSellerFee(int grossAmount) {
        return calculateSellerFee(grossAmount, SELLER_FEE_RATE_24_HOURS_BPS);
    }

    /**
     * Calcula a comissão usando uma taxa em basis points.
     *
     * @param grossAmount valor bruto da venda
     * @param feeRateBps taxa em basis points, onde 10.000 representa 100%
     * @return comissão em Bits, arredondada para baixo
     */
    public static int calculateSellerFee(int grossAmount, int feeRateBps) {
        if (grossAmount <= 0 || feeRateBps <= 0) {
            return 0;
        }
        return (int) ((long) grossAmount * feeRateBps / 10000);
    }

    /**
     * Obtém a comissão aplicável à duração do anúncio.
     *
     * @param durationHours duração em horas
     * @return taxa em basis points
     * @throws BadRequestException quando a duração não é permitida
     */
    public static int sellerFeeRateBpsForDuration(int durationHours) {
        return switch (durationHours) {
            case 24 -> SELLER_FEE_RATE_24_HOURS_BPS;
            case 48 -> SELLER_FEE_RATE_48_HOURS_BPS;
            case 72 -> SELLER_FEE_RATE_72_HOURS_BPS;
            default -> throw new BadRequestException("Duration must be 24, 48 or 72 hours");
        };
    }

    /**
     * Calcula o instante de expiração a partir da publicação.
     *
     * @param createdAt instante de criação do anúncio
     * @param durationHours duração autorizada
     * @return instante em que o anúncio deixa de aceitar compras
     */
    public static Instant expirationAt(Instant createdAt, int durationHours) {
        return createdAt.plus(Duration.ofHours(durationHours));
    }

    private AuctionRules() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
