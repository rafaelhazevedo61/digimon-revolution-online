package com.dro.modules.digimon.domain;

import com.dro.modules.digimon.domain.enums.DigimonGrade;

/**
 * Componente da camada de conjunto de regras de domínio do módulo de Digimon.
 */
public class DigimonGradeRules {

    private DigimonGradeRules() {
    }

    public static DigimonGrade calculate(int ivHp, int ivAttack, int ivDefense) {

        int perfectIvs = countPerfectIvs(ivHp, ivAttack, ivDefense);

        if (perfectIvs == 3) {
            return DigimonGrade.SSS;
        }

        if (perfectIvs == 2) {
            return DigimonGrade.SS;
        }

        if (perfectIvs == 1) {
            return DigimonGrade.S;
        }

        int average = calculateAverage(ivHp, ivAttack, ivDefense);

        if (average >= 85) {
            return DigimonGrade.A;
        }

        if (average >= 70) {
            return DigimonGrade.B;
        }

        if (average >= 55) {
            return DigimonGrade.C;
        }

        if (average >= 40) {
            return DigimonGrade.D;
        }

        return DigimonGrade.E;
    }

    private static int countPerfectIvs(int ivHp, int ivAttack, int ivDefense) {
        int count = 0;

        if (ivHp == 100) {
            count++;
        }

        if (ivAttack == 100) {
            count++;
        }

        if (ivDefense == 100) {
            count++;
        }

        return count;
    }

    private static int calculateAverage(int ivHp, int ivAttack, int ivDefense) {
        return (ivHp + ivAttack + ivDefense) / 3;
    }
}