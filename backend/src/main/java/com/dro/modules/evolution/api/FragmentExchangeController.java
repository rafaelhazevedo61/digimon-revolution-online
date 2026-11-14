package com.dro.modules.evolution.api;

import com.dro.modules.evolution.application.ExchangeEvolutionFragments;
import com.dro.shared.util.TokenExtractor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/digimon/{digimonId}/fragment-exchange/{lineId}")
public class FragmentExchangeController {
    private final ExchangeEvolutionFragments exchange;

    public FragmentExchangeController(ExchangeEvolutionFragments exchange) {
        this.exchange = exchange;
    }

    @GetMapping
    public ExchangeEvolutionFragments.Offer offer(@RequestHeader("Authorization") String token,
                                                   @PathVariable UUID digimonId, @PathVariable long lineId) {
        return exchange.offer(TokenExtractor.extractPlayerId(token), digimonId, lineId);
    }

    @PostMapping
    public ExchangeEvolutionFragments.Result exchange(@RequestHeader("Authorization") String token,
                                                       @PathVariable UUID digimonId, @PathVariable long lineId,
                                                       @Valid @RequestBody Request request) {
        return exchange.exchange(TokenExtractor.extractPlayerId(token), digimonId, lineId, request.inputs());
    }

    public record Request(@NotEmpty Map<@NotBlank String, @NotNull @Positive Integer> inputs) {}
}
