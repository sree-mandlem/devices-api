package com.company.devices.api.dto;

import com.company.devices.domain.DeviceState;

import java.time.Instant;

public record DeviceResponse(
        Long id,
        String name,
        String brand,
        DeviceState state,
        Instant creationTime
) {}
