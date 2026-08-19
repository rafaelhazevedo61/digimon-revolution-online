package com.dro.modules.digimon.infra.spec;

import com.dro.modules.digimon.domain.DigimonInfos;
import org.springframework.data.jpa.domain.Specification;

/**
 * Componente da camada de modelo de domínio do módulo de Digimon.
 */
public class DigimonInfosSpecifications {

    private DigimonInfosSpecifications() {
    }

    public static Specification<DigimonInfos> withFilters(
            String name,
            String stage,
            String attribute,
            String element,
            String specie
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (hasText(name)) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.like(
                                criteriaBuilder.upper(root.get("name")),
                                "%" + name.trim().toUpperCase() + "%"
                        )
                );
            }

            if (hasText(stage)) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(
                                criteriaBuilder.upper(root.get("stage").as(String.class)),
                                stage.trim().toUpperCase()
                        )
                );
            }

            if (hasText(attribute)) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(
                                criteriaBuilder.upper(root.get("attribute").as(String.class)),
                                attribute.trim().toUpperCase()
                        )
                );
            }

            if (hasText(element)) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(
                                criteriaBuilder.upper(root.get("element").as(String.class)),
                                element.trim().toUpperCase()
                        )
                );
            }

            if (hasText(specie)) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(
                                criteriaBuilder.upper(root.get("specie").as(String.class)),
                                specie.trim().toUpperCase()
                        )
                );
            }

            return predicates;
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}