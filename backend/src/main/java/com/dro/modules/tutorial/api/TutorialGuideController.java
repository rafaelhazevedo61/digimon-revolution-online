package com.dro.modules.tutorial.api;

import com.dro.modules.tutorial.api.dto.response.TutorialGuideResponse;
import com.dro.modules.tutorial.application.TutorialGuideService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tutorial/guide")
public class TutorialGuideController {
    private final TutorialGuideService service;
    public TutorialGuideController(TutorialGuideService service) { this.service = service; }
    @GetMapping
    public TutorialGuideResponse page(@RequestHeader("Authorization") String token) { return service.page(token); }
    @PostMapping("/{topic}/acknowledge")
    public TutorialGuideResponse acknowledge(@RequestHeader("Authorization") String token, @PathVariable String topic) {
        return service.acknowledge(token, topic);
    }
}
