package com.dro.modules.mission.api;

import com.dro.modules.mission.api.dto.response.AreaResponse;
import com.dro.modules.mission.application.AreaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/areas")
@RequiredArgsConstructor
public class AreaController {

    private final AreaUseCase areaUseCase;

    @GetMapping
    public List<AreaResponse> getAreas(
            @RequestHeader("Authorization") String token
    ) {
        return areaUseCase.execute(token);
    }
}
