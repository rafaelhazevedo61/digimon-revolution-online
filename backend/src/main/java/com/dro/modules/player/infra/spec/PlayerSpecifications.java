package com.dro.modules.player.infra.spec;

import com.dro.modules.digitama.domain.enums.DigitamaType;
import com.dro.modules.player.domain.Player;
import com.dro.shared.exception.BadRequestException;
import org.springframework.data.jpa.domain.Specification;

/**
 * Componente da camada de modelo de domínio do módulo de Jogadores.
 */
public class PlayerSpecifications {

    private PlayerSpecifications() {
    }

    public static Specification<Player> withFilters(
            String username,
            String email,
            String selectedDigitama,
            Boolean starterSelected
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (hasText(username)) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.like(
                                criteriaBuilder.upper(root.get("username")),
                                "%" + username.trim().toUpperCase() + "%"
                        )
                );
            }

            if (hasText(email)) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.like(
                                criteriaBuilder.upper(root.get("email")),
                                "%" + email.trim().toUpperCase() + "%"
                        )
                );
            }

            if (hasText(selectedDigitama)) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(
                                root.get("selectedDigitama"),
                                parseDigitamaType(selectedDigitama)
                        )
                );
            }

            if (starterSelected != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(root.get("starterSelected"), starterSelected)
                );
            }

            return predicates;
        };
    }

    private static DigitamaType parseDigitamaType(String value) {
        try {
            return DigitamaType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Invalid selectedDigitama: " + value);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}