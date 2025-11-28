package com.company.devices.service;

import com.company.devices.api.dto.DeviceCreateRequest;
import com.company.devices.api.dto.DevicePatchRequest;
import com.company.devices.api.dto.DeviceResponse;
import com.company.devices.api.dto.DeviceUpdateRequest;
import com.company.devices.domain.Device;
import com.company.devices.domain.exception.DeviceNotFoundException;
import com.company.devices.domain.exception.InvalidDeviceOperationException;
import com.company.devices.repository.DeviceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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
        var saved = Device.builder().id(1L).name("iPhone").brand("Apple").state(AVAILABLE).build();
        var response = new DeviceResponse(1L, "iPhone", "Apple", AVAILABLE, now());
        when(mapper.toEntity(request)).thenReturn(device);
        when(repository.save(device)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        var result = service.create(request);

        assertThat(result).isEqualTo(response);
        verify(mapper).toEntity(request);
        verify(repository).save(device);
        verify(mapper).toResponse(saved);
    }

    @Test
    @DisplayName("getById() should return mapped response when device exists")
    void getById_shouldReturnResponseWhenFound() {
        var id = 10L;
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
    @DisplayName("getById() should throw when device is missing")
    void getById_shouldThrowWhenNotFound() {
        var id = 404L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessage("Device not found with id: " + id);
        verify(repository).findById(id);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("delete() should throw when device is missing")
    void delete_shouldThrowWhenNotFound() {
        var id = 404L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessage("Device not found with id: " + id);
        verify(repository).findById(id);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("delete() should delete device when it is not IN_USE")
    void delete_shouldRemoveEntityWhenNotInUse() {
        var id = 3L;
        var device = Device.builder().id(id).name("iPhone").brand("Apple").state(AVAILABLE).build();
        when(repository.findById(id)).thenReturn(Optional.of(device));

        service.delete(id);

        verify(repository).findById(id);
        verify(repository).delete(device);
    }

    @Test
    @DisplayName("delete() should throw when device is IN_USE")
    void delete_shouldThrowWhenInUse() {
        var id = 5L;
        var device = Device.builder().id(id).name("iPhone").brand("Apple").state(IN_USE).build();
        when(repository.findById(id)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(InvalidDeviceOperationException.class)
                .hasMessageContaining("Cannot delete device that is currently IN_USE");
        verify(repository).findById(id);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("update() should update fields when device is AVAILABLE(not IN_USE)")
    void update_shouldModifyFieldsWhenAvailable() {
        var id = 11L;
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
        var id = 11L;
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
    @DisplayName("update() should throw when entity is missing")
    void update_shouldThrowWhenNotFound() {
        var id = 404L;
        var request = new DeviceUpdateRequest("Name", "Brand", AVAILABLE);
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, request))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessage("Device not found with id: " + id);
        verify(repository).findById(id);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("update() should allow updating only state when device is IN_USE")
    void update_shouldAllowOnlyStateChangeWhenInUse() {
        var id = 20L;
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
    @DisplayName("update() should throw when name or brand changes while IN_USE")
    void update_shouldThrowWhenChangingNameOrBrandWhileInUse() {
        var id = 400L;
        var existing = Device.builder().id(id).name("Locked name").brand("Locked brand").state(IN_USE).build();
        var request = new DeviceUpdateRequest("New name", "Locked brand", IN_USE);

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(id, request))
                .isInstanceOf(InvalidDeviceOperationException.class)
                .hasMessage("Name and brand cannot be updated when device is IN_USE");
        verify(repository).findById(id);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("patch should update all provided fields when device is not IN_USE")
    void patch_shouldUpdateAllProvidedFields_whenNotInUse() {
        var id = 10L;
        var existing = Device.builder().id(id).name("Old name").brand("Old brand").state(AVAILABLE).build();
        var request = new DevicePatchRequest("New name", "New brand", IN_USE);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        var expectedResponse = new DeviceResponse(id, "New name", "New brand", IN_USE, now());
        when(mapper.toResponse(existing)).thenReturn(expectedResponse);

        var result = service.patch(id, request);

        assertThat(existing.getName()).isEqualTo("New name");
        assertThat(existing.getBrand()).isEqualTo("New brand");
        assertThat(existing.getState()).isEqualTo(IN_USE);
        assertThat(result).isSameAs(expectedResponse);
        verify(repository).findById(id);
        verify(mapper).toResponse(existing);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    @DisplayName("patch should update only fields that are non-null in request")
    void patch_shouldUpdateOnlyNonNullFields() {
        var id = 11L;
        var existing = Device.builder().id(id).name("Name").brand("Brand").state(AVAILABLE).build();
        var request = new DevicePatchRequest(null, null, IN_USE);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        var expectedResponse = new DeviceResponse(id, "Name", "Brand", IN_USE, now());
        when(mapper.toResponse(existing)).thenReturn(expectedResponse);

        var result = service.patch(id, request);

        assertThat(existing.getName()).isEqualTo("Name");
        assertThat(existing.getBrand()).isEqualTo("Brand");
        assertThat(existing.getState()).isEqualTo(IN_USE);
        assertThat(result).isSameAs(expectedResponse);
        verify(repository).findById(id);
        verify(mapper).toResponse(existing);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    @DisplayName("patch should throw when device is IN_USE and name or brand is changed")
    void patch_shouldThrowWhenInUseAndNameOrBrandChanged() {
        var id = 12L;
        var existing = Device.builder().id(id).name("Name").brand("Brand").state(IN_USE).build();
        var request = new DevicePatchRequest("Changed name", null, null);
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.patch(id, request))
                .isInstanceOf(InvalidDeviceOperationException.class)
                .hasMessage("Name and brand cannot be updated when device is IN_USE");
        assertThat(existing.getName()).isEqualTo("Name");
        assertThat(existing.getBrand()).isEqualTo("Brand");
        assertThat(existing.getState()).isEqualTo(IN_USE);
        verify(repository).findById(id);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("patch should allow changing state when device is IN_USE but name and brand do not change")
    void patch_shouldAllowStateChangeWhenInUseAndNameBrandUnchanged() {
        var id = 13L;
        var existing = Device.builder().id(id).name("Name").brand("Brand").state(IN_USE).build();
        var request = new DevicePatchRequest(null, null, AVAILABLE);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        var expectedResponse = new DeviceResponse(id, "Name", "Brand", AVAILABLE, now());
        when(mapper.toResponse(existing)).thenReturn(expectedResponse);

        var result = service.patch(id, request);

        assertThat(existing.getName()).isEqualTo("Name");
        assertThat(existing.getBrand()).isEqualTo("Brand");
        assertThat(existing.getState()).isEqualTo(AVAILABLE);
        assertThat(result).isSameAs(expectedResponse);
        verify(repository).findById(id);
        verify(mapper).toResponse(existing);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    @DisplayName("patch should throw when device does not exist")
    void patch_shouldThrowWhenDeviceNotFound() {
        var id = 999L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.patch(id, new DevicePatchRequest(null, null, null)))
                .isInstanceOf(DeviceNotFoundException.class)
                .hasMessage("Device not found with id: " + id);
        verify(repository).findById(id);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(mapper);
    }

    @Test
    void getByBrand_shouldReturnMappedResponses() {
        var apple1 = Device.builder().id(1L).name("IPhone").brand("Apple").build();
        var apple2 = Device.builder().id(1L).name("Macbook").brand("Apple").build();
        var response1 = new DeviceResponse(1L, "iPhone", "Apple", null, now());
        var response2 = new DeviceResponse(2L, "MacBook", "Apple", null, now());
        when(repository.findByBrandIgnoreCase("Apple")).thenReturn(List.of(apple1, apple2));
        when(mapper.toResponse(apple1)).thenReturn(response1);
        when(mapper.toResponse(apple2)).thenReturn(response2);

        var result = service.getByBrand("Apple");

        assertThat(result).containsExactly(response1, response2);
        verify(repository).findByBrandIgnoreCase("Apple");
        verify(mapper).toResponse(apple1);
        verify(mapper).toResponse(apple2);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    void getByBrand_shouldReturnEmptyList_whenNoDevicesFound() {
        when(repository.findByBrandIgnoreCase("Samsung")).thenReturn(List.of());

        var result = service.getByBrand("Samsung");

        assertThat(result).isEmpty();
        verify(repository).findByBrandIgnoreCase("Samsung");
        verifyNoInteractions(mapper); // mapper should never be called
    }

    @Test
    void getByBrand_shouldCallIgnoreCaseRepositoryMethod() {
        var apple1 = Device.builder().id(1L).name("IPhone").brand("Apple").build();
        var response1 = new DeviceResponse(1L, "iPhone", "Apple", null, now());
        when(repository.findByBrandIgnoreCase("apple")).thenReturn(List.of(apple1));
        when(mapper.toResponse(apple1)).thenReturn(response1);

        var result = service.getByBrand("apple");

        assertThat(result).containsExactly(response1);
        // ensure the exact input (lowercase) was used
        var captor = ArgumentCaptor.forClass(String.class);
        verify(repository).findByBrandIgnoreCase(captor.capture());
        assertThat(captor.getValue()).isEqualTo("apple");
    }

    @Test
    void getByBrand_shouldThrowException_whenBrandIsNull() {
        assertThatThrownBy(() -> service.getByBrand(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Brand cannot be empty");

        verifyNoInteractions(repository);
        verifyNoInteractions(mapper);
    }


    @Test
    void getByBrand_shouldThrowException_whenBrandIsEmpty() {
        assertThatThrownBy(() -> service.getByBrand(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Brand cannot be empty");

        verifyNoInteractions(repository);
        verifyNoInteractions(mapper);
    }
}