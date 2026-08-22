package com.dro.modules.tutorial.api;

import com.dro.modules.tutorial.api.dto.response.TutorialProgressResponse;
import com.dro.modules.tutorial.application.TutorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Componente da camada de controller da API do módulo de Tutorial.
 */
@RestController
@RequestMapping("/tutorial")
@RequiredArgsConstructor
public class TutorialController {

    private final TutorialService tutorialService;

    @GetMapping
    public ResponseEntity<TutorialProgressResponse> getProgress(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(tutorialService.getProgress(authorization));
    }

    @PostMapping("/steps/{step}/claim")
    public ResponseEntity<TutorialProgressResponse> claimReward(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String step
    ) {
        return ResponseEntity.ok(tutorialService.claimReward(authorization, step));
    }

    @PostMapping("/finish")
    public ResponseEntity<TutorialProgressResponse> finishTutorial(
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(tutorialService.finishTutorial(authorization));
    }
}
