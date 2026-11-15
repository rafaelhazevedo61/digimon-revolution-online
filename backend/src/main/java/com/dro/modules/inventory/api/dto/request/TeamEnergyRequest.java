package com.dro.modules.inventory.api.dto.request;

import com.dro.modules.inventory.domain.ItemType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record TeamEnergyRequest(@NotNull ItemType itemType, @NotNull @Size(min = 1, max = 3) List<@NotNull UUID> memberIds) {}
