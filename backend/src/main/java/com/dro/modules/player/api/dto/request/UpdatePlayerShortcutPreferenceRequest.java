package com.dro.modules.player.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdatePlayerShortcutPreferenceRequest(@NotNull @Valid List<String> routes) {
}

