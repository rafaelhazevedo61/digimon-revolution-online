package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.Stage;

public class RebirthRules {

    /*
     * Valor máximo permitido para qualquer IV.
     *
     * Como os IVs do projeto agora vão de 0 até 100,
     * nenhuma regra de Rebirth pode gerar valor acima disso.
     */
    private static final int MAX_IV = 100;

    /*
     * Quantidade de IV mínimo adicionada por Rebirth.
     *
     * Exemplo:
     * Rebirth 1 = +3 no IV mínimo
     * Rebirth 2 = +6 no IV mínimo
     * Rebirth 10 = +30 no IV mínimo
     */
    private static final int IV_BONUS_PER_REBIRTH = 3;

    /*
     * Limite máximo de bônus de IV mínimo vindo apenas de Rebirth.
     *
     * Mesmo que o Digimon tenha mais de 10 Rebirths,
     * esse bônus direto não passa de +30.
     *
     * Isso permite Rebirth infinito sem quebrar o balanceamento.
     */
    private static final int MAX_REBIRTH_IV_BONUS = 30;

    /*
     * Bônus percentual de status por Rebirth.
     *
     * Cada Rebirth concede +2% de HP, ATK e DEF.
     */
    private static final double STAT_BONUS_PER_REBIRTH = 0.02;

    /*
     * Limite máximo do bônus percentual de status.
     *
     * Com esse cap, o bônus direto de status chega a +100%,
     * equivalente ao multiplicador final x2,00.
     * Como cada Rebirth concede +2%, o limite é alcançado no 50º Rebirth.
     */
    private static final double MAX_STAT_BONUS = 1.00;

    /*
     * Calcula o bônus de IV mínimo obtido pela quantidade de Rebirths.
     *
     * Fórmula:
     * rebirthCount * 3
     *
     * Com limite máximo de +30.
     *
     * Exemplo:
     * rebirthCount = 1  -> +3
     * rebirthCount = 5  -> +15
     * rebirthCount = 10 -> +30
     * rebirthCount = 20 -> +30
     */
    public static int calculateIvBonus(int rebirthCount) {
        return Math.min(
                rebirthCount * IV_BONUS_PER_REBIRTH,
                MAX_REBIRTH_IV_BONUS
        );
    }

    /*
     * Calcula o multiplicador final de status vindo do Rebirth.
     *
     * Cada Rebirth concede +2% de status, limitado ao multiplicador x2,00.
     *
     * Fórmula:
     * 1.0 + min(rebirthCount * 0.02, 1.00)
     *
     * Exemplo:
     * rebirthCount = 0  -> 1.00
     * rebirthCount = 1  -> 1.02
     * rebirthCount = 5  -> 1.10
     * rebirthCount = 10 -> 1.20
     * rebirthCount = 25 -> 1.50
     * rebirthCount = 50 -> 2.00
     * rebirthCount = 60 -> 2.00
     */
    public static double calculateStatMultiplier(int rebirthCount) {
        double bonus = Math.min(
                rebirthCount * STAT_BONUS_PER_REBIRTH,
                MAX_STAT_BONUS
        );

        return 1.0 + bonus;
    }

    /*
     * Calcula o IV mínimo que o novo Digimon poderá receber
     * em um atributo específico após o Rebirth.
     *
     * Essa regra NÃO aumenta o IV antigo diretamente.
     *
     * Em vez disso, ela define o piso mínimo do novo sorteio.
     *
     * Exemplo:
     * IV antigo = 70
     * Resultado NÃO é 73 fixo.
     * Resultado é um novo IV sorteado entre o mínimo calculado e 100.
     *
     * Parâmetros:
     * previousIv       -> IV do Digimon antes do Rebirth
     * rarityMinimumIv  -> IV mínimo base definido pela raridade sorteada
     * rebirthCount     -> quantidade de Rebirths do novo ciclo
     *
     * Regras especiais:
     *
     * 1. Se o IV antigo era 100:
     *    O novo Digimon terá mínimo 90 nesse atributo.
     *
     * 2. Se o IV antigo era entre 90 e 99:
     *    O novo Digimon terá mínimo 75 nesse atributo.
     *
     * 3. Para os demais casos:
     *    O sistema usa o maior valor entre:
     *
     *    a) IV mínimo da raridade + bônus de Rebirth
     *    b) 50% do IV antigo
     *
     * Fórmula geral:
     * max(
     *   rarityMinimumIv + rebirthBonus,
     *   previousIv / 2
     * )
     *
     * O retorno nunca ultrapassa 100.
     */
    public static int calculateInheritedIvMinimum(
            int previousIv,
            int rarityMinimumIv,
            int rebirthCount
    ) {

        if (previousIv >= 100) {
            return 90;
        }

        if (previousIv >= 90) {
            return 75;
        }

        int rebirthBonus = calculateIvBonus(rebirthCount);

        int rarityBasedMinimum = rarityMinimumIv + rebirthBonus;

        int previousIvProtection = previousIv / 2;

        int inheritedMinimum = Math.max(
                rarityBasedMinimum,
                previousIvProtection
        );

        return Math.min(MAX_IV, inheritedMinimum);
    }

    /*
     * Calcula o custo em Bits para realizar Rebirth.
     *
     * Como os Bits ficarão na entidade Digimon,
     * esse valor deve ser debitado do próprio Digimon que será renascido.
     *
     * Fórmula:
     * 10.000 * (rebirthCount + 1)
     *
     * Exemplo:
     * rebirthCount = 0 -> primeiro Rebirth custa 10.000
     * rebirthCount = 1 -> segundo Rebirth custa 20.000
     * rebirthCount = 2 -> terceiro Rebirth custa 30.000
     */
    public static int calculateBitsCost(int rebirthCount) {
        return 10_000 * (rebirthCount + 1);
    }

    /*
     * Calcula o custo em Data Core.
     *
     * Fórmula:
     * rebirthCount + 1
     *
     * Exemplo:
     * rebirthCount = 0 -> primeiro Rebirth custa 1 Data Core
     * rebirthCount = 1 -> segundo Rebirth custa 2 Data Cores
     */
    public static int calculateDataCoreCost(int rebirthCount) {
        return rebirthCount + 1;
    }

    /*
     * Define se o estágio atual do Digimon permite Rebirth.
     *
     * Regra atual:
     * Apenas Champion ou superior pode fazer Rebirth.
     *
     * Como o enum Stage está em ordem evolutiva:
     * BABY -> ROOKIE -> CHAMPION -> ULTIMATE -> MEGA
     *
     * Podemos comparar usando ordinal().
     */
    public static boolean isEligibleStage(Stage stage) {
        return stage.ordinal() >= Stage.CHAMPION.ordinal();
    }
}