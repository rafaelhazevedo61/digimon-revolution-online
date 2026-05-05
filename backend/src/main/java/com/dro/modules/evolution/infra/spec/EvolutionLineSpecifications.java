package com.dro.modules.evolution.infra.spec;

import com.dro.modules.evolution.domain.EvolutionLine;
import org.springframework.data.jpa.domain.Specification;

public class EvolutionLineSpecifications {

    private EvolutionLineSpecifications() {
    }

    public static Specification<EvolutionLine> withFilters(
            String code,
            String name,
            Boolean active
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (hasText(code)) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.like(
                                criteriaBuilder.upper(root.get("code")),
                                "%" + code.trim().toUpperCase() + "%"
                        )
                );
            }

            if (hasText(name)) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.like(
                                criteriaBuilder.upper(root.get("name")),
                                "%" + name.trim().toUpperCase() + "%"
                        )
                );
            }

            if (active != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(root.get("active"), active)
                );
            }

            return predicates;
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}