package com.company.devices.service;

import com.company.devices.api.dto.DeviceCreateRequest;
import com.company.devices.api.dto.DeviceResponse;
import com.company.devices.domain.Device;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    DeviceResponse toResponse(Device device);

    Device toEntity(DeviceCreateRequest request);
}

