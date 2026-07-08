package com.dro.modules.tutorial.api;

import com.dro.modules.tutorial.api.dto.response.TutorialProgressResponse;
import com.dro.modules.tutorial.application.TutorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
