package com.dro.modules.config.api;

import com.dro.modules.config.api.dto.response.PublicConfigResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicConfigController {

    private final boolean registrationInviteRequired;

    public PublicConfigController(
            @Value("${dro.registration.invite-required:true}")
            boolean registrationInviteRequired
    ) {
        this.registrationInviteRequired =
                registrationInviteRequired;
    }

    @GetMapping("/config")
    public PublicConfigResponse getConfig() {
        return new PublicConfigResponse(
                registrationInviteRequired
        );
    }
}