package com.company.devices.service;

import com.company.devices.api.dto.DeviceCreateRequest;
import com.company.devices.api.dto.DeviceResponse;
import com.company.devices.api.dto.DeviceUpdateRequest;
import com.company.devices.domain.Device;
import com.company.devices.repository.DeviceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.company.devices.domain.DeviceState.*;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository repository;

    @Mock
    private DeviceMapper mapper;

    @InjectMocks
    private DeviceService service;

    @Test
    @DisplayName("create() should map request, save entity and return response")
    void create_shouldPersistAndReturnResponse() {
        var request = new DeviceCreateRequest("iPhone", "Apple", AVAILABLE);
        var device = Device.builder().name("iPhone").brand("Apple").state(AVAILABLE).build();
        var savedEntity = Device.builder().id(1L).name("iPhone").brand("Apple").state(AVAILABLE).build();
        var response = new DeviceResponse(1L, "iPhone", "Apple", AVAILABLE, now());
        when(mapper.toEntity(request)).thenReturn(device);
        when(repository.save(device)).thenReturn(savedEntity);
        when(mapper.toResponse(savedEntity)).thenReturn(response);

        var result = service.create(request);

        assertThat(result).isEqualTo(response);
        verify(mapper).toEntity(request);
        verify(repository).save(device);
        verify(mapper).toResponse(savedEntity);
    }

    @Test
    @DisplayName("getById() should return mapped response when entity exists")
    void getById_shouldReturnResponseWhenFound() {
        Long id = 10L;
        var device = Device.builder().id(id).name("iPhone").brand("Apple").state(AVAILABLE).build();
        var response = new DeviceResponse(id, "Kindle", "Amazon", AVAILABLE, now());

        when(repository.findById(id)).thenReturn(Optional.of(device));
        when(mapper.toResponse(device)).thenReturn(response);

        var result = service.getById(id);

        assertThat(result).isEqualTo(response);
        verify(repository).findById(id);
        verify(mapper).toResponse(device);
    }

    @Test
    @DisplayName("getById() should throw EntityNotFoundException when entity is missing")
    void getById_shouldThrowWhenNotFound() {
        Long id = 404L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Retrieval failed")
                .hasMessageContaining(String.valueOf(id));
        verify(repository).findById(id);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("delete() should throw EntityNotFoundException when entity is missing")
    void delete_shouldThrowWhenNotFound() {
        Long id = 404L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Deletion failed")
                .hasMessageContaining(String.valueOf(id));
        verify(repository).findById(id);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("delete() should delete entity when it is not IN_USE")
    void delete_shouldRemoveEntityWhenNotInUse() {
        Long id = 3L;
        var device = Device.builder().id(id).name("iPhone").brand("Apple").state(AVAILABLE).build();
        when(repository.findById(id)).thenReturn(Optional.of(device));

        service.delete(id);

        verify(repository).findById(id);
        verify(repository).delete(device);
    }

    @Test
    @DisplayName("delete() should throw IllegalStateException when device is IN_USE")
    void delete_shouldThrowWhenInUse() {
        Long id = 5L;
        var device = Device.builder().id(id).name("iPhone").brand("Apple").state(IN_USE).build();
        when(repository.findById(id)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete device that is currently IN_USE");
        verify(repository).findById(id);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("update() should update fields when device is AVAILABLE(not IN_USE)")
    void update_shouldModifyFieldsWhenAvailable() {
        Long id = 11L;
        var existing = Device.builder().id(id).name("Old name").brand("Old brand").state(AVAILABLE).build();
        var request = new DeviceUpdateRequest("New name", "New brand", INACTIVE);
        var expectedResponse = new DeviceResponse(id, "New name", "New brand", INACTIVE, now());
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(mapper.toResponse(existing)).thenReturn(expectedResponse);

        var result = service.update(id, request);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(existing.getName()).isEqualTo("New name");
        assertThat(existing.getBrand()).isEqualTo("New brand");
        assertThat(existing.getState()).isEqualTo(INACTIVE);
        verify(repository).findById(id);
        verify(mapper).toResponse(existing);
    }

    @Test
    @DisplayName("update() should update fields when device is INACTIVE(not IN_USE)")
    void update_shouldModifyFieldsWhenInActive() {
        Long id = 11L;
        var existing = Device.builder().id(id).name("Old name").brand("Old brand").state(INACTIVE).build();
        var request = new DeviceUpdateRequest("New name", "New brand", INACTIVE);
        var expectedResponse = new DeviceResponse(id, "New name", "New brand", INACTIVE, now());
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(mapper.toResponse(existing)).thenReturn(expectedResponse);

        var result = service.update(id, request);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(existing.getName()).isEqualTo("New name");
        assertThat(existing.getBrand()).isEqualTo("New brand");
        assertThat(existing.getState()).isEqualTo(INACTIVE);
        verify(repository).findById(id);
        verify(mapper).toResponse(existing);
    }

    @Test
    @DisplayName("update() should throw EntityNotFoundException when entity is missing")
    void update_shouldThrowWhenNotFound() {
        Long id = 404L;
        var request = new DeviceUpdateRequest("Name", "Brand", AVAILABLE);
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Retrieval failed")
                .hasMessageContaining(String.valueOf(id));
        verify(repository).findById(id);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("update() should allow updating only state when device is IN_USE")
    void update_shouldAllowOnlyStateChangeWhenInUse() {
        Long id = 20L;
        var existing = Device.builder().id(id).name("Name").brand("Brand").state(IN_USE).build();
        var request = new DeviceUpdateRequest("Name", "Brand", AVAILABLE);
        var expectedResponse = new DeviceResponse(id, "Name", "Brand", AVAILABLE, now());
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(mapper.toResponse(existing)).thenReturn(expectedResponse);

        var result = service.update(id, request);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(existing.getName()).isEqualTo("Name");
        assertThat(existing.getBrand()).isEqualTo("Brand");
        assertThat(existing.getState()).isEqualTo(AVAILABLE);
        verify(repository).findById(id);
        verify(mapper).toResponse(existing);
    }

    @Test
    @DisplayName("update() should throw IllegalStateException when name or brand changes while IN_USE")
    void update_shouldThrowWhenChangingNameOrBrandWhileInUse() {
        Long id = 400L;
        var existing = Device.builder().id(id).name("Locked name").brand("Locked brand").state(IN_USE).build();
        var request = new DeviceUpdateRequest("New name", "Locked brand", IN_USE);

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(id, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Name and brand cannot be updated when device is IN_USE");
        verify(repository).findById(id);
        verifyNoInteractions(mapper);
    }
}