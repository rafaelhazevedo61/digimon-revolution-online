package com.dro.modules.mission.api;

import com.dro.modules.mission.api.dto.response.AreaResponse;
import com.dro.modules.mission.application.AreaUseCase;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Componente da camada de controller da API do módulo de Missões.
 */
@RestController
@RequestMapping("/areas")
public class AreaController {
    private final AreaUseCase areaUseCase;

    @GetMapping
    public List<AreaResponse> getAreas(@RequestHeader("Authorization") String token) {
        return areaUseCase.execute(token);
    }

    public AreaController(final AreaUseCase areaUseCase) {
        this.areaUseCase = areaUseCase;
    }
}
