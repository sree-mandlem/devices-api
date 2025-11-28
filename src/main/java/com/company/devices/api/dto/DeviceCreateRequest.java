package com.company.devices.api.dto;

import com.company.devices.domain.DeviceState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceCreateRequest(
        @NotBlank String name,
        @NotBlank String brand,
        @NotNull DeviceState state
) {}

