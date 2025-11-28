package com.company.devices.api.dto;

import com.company.devices.domain.DeviceState;

public record DevicePatchRequest(
        String name,
        String brand,
        DeviceState state
) {}
