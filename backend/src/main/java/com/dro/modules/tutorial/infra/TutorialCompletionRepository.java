package com.dro.modules.tutorial.infra;

import com.dro.modules.tutorial.domain.TutorialCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TutorialCompletionRepository extends JpaRepository<TutorialCompletion, UUID> {
}
