package com.company.devices.service;

import com.company.devices.api.dto.DeviceCreateRequest;
import com.company.devices.api.dto.DeviceResponse;
import com.company.devices.domain.Device;
import com.company.devices.domain.DeviceState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static com.company.devices.domain.DeviceState.AVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;

class DeviceMapperTest {

    private final DeviceMapper mapper = Mappers.getMapper(DeviceMapper.class);

    @Test
    @DisplayName("toEntity() should map all fields from DeviceCreateRequest to Device except creationTime")
    void toEntity_shouldMapFields() {
        var request = new DeviceCreateRequest("iPhone", "Apple", AVAILABLE);

        var result = mapper.toEntity(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).as("id should not be set by mapper").isNull();
        assertThat(result.getName()).isEqualTo("iPhone");
        assertThat(result.getBrand()).isEqualTo("Apple");
        assertThat(result.getState()).isEqualTo(AVAILABLE);
        // creationTime is set by @PrePersist, not by mapper
        assertThat(result.getCreationTime()).as("creationTime should not be set by mapper").isNull();
    }

    @Test
    @DisplayName("toResponse() should map all fields from Device to DeviceResponse")
    void toResponse_shouldMapFields() {
        var now = Instant.now();
        var device = Device.builder()
                .id(42L)
                .name("Kindle")
                .brand("Amazon")
                .state(DeviceState.IN_USE)
                .creationTime(now)
                .build();

        DeviceResponse result = mapper.toResponse(device);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(42L);
        assertThat(result.name()).isEqualTo("Kindle");
        assertThat(result.brand()).isEqualTo("Amazon");
        assertThat(result.state()).isEqualTo(DeviceState.IN_USE);
        assertThat(result.creationTime()).isEqualTo(now);
    }

    @Test
    @DisplayName("Mapper should handle null inputs gracefully by returning null")
    void mapper_shouldHandleNullInputs() {
        assertThat(mapper.toEntity((null))).isNull();
        assertThat(mapper.toResponse(null)).isNull();
    }
}