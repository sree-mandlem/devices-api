package com.company.devices.service;

import com.company.devices.api.dto.DeviceCreateRequest;
import com.company.devices.api.dto.DevicePatchRequest;
import com.company.devices.api.dto.DeviceResponse;
import com.company.devices.api.dto.DeviceUpdateRequest;
import com.company.devices.domain.Device;
import com.company.devices.domain.DeviceState;
import com.company.devices.domain.exception.DeviceNotFoundException;
import com.company.devices.domain.exception.InvalidDeviceOperationException;
import com.company.devices.repository.DeviceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
        var request = new DeviceCreateRequest("IPhone", "Apple", AVAILABLE);
        var device = Device.builder().name("IPhone").brand("Apple").state(AVAILABLE).build();
        var saved = Device.builder().id(1L).name("IPhone").brand("Apple").state(AVAILABLE).build();
        var response = new DeviceResponse(1L, "IPhone", "Apple", AVAILABLE, now());
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
    @DisplayName("getById() should return mapped response when device exists with given id")
    void getById_shouldReturnResponseWhenFound() {
        var id = 10L;
        var device = Device.builder().id(id).name("IPhone").brand("Apple").state(AVAILABLE).build();
        var response = new DeviceResponse(id, "IPhone", "Apple", AVAILABLE, now());

        when(repository.findById(id)).thenReturn(Optional.of(device));
        when(mapper.toResponse(device)).thenReturn(response);

        var result = service.getById(id);

        assertThat(result).isEqualTo(response);
        verify(repository).findById(id);
        verify(mapper).toResponse(device);
    }

    @Test
    @DisplayName("getById() should throw when device does not exist with given id")
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
    @DisplayName("delete() should throw when device does not exist with given id")
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
    @DisplayName("delete() should delete device when the device is not IN_USE")
    void delete_shouldRemoveEntityWhenNotInUse() {
        var id = 3L;
        var device = Device.builder().id(id).name("IPhone").brand("Apple").state(AVAILABLE).build();
        when(repository.findById(id)).thenReturn(Optional.of(device));

        service.delete(id);

        verify(repository).findById(id);
        verify(repository).delete(device);
    }

    @Test
    @DisplayName("delete() should throw when the device is IN_USE")
    void delete_shouldThrowWhenInUse() {
        var id = 5L;
        var device = Device.builder().id(id).name("IPhone").brand("Apple").state(IN_USE).build();
        when(repository.findById(id)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(InvalidDeviceOperationException.class)
                .hasMessageContaining("Cannot delete device that is currently IN_USE");
        verify(repository).findById(id);
        verify(repository, never()).delete(any());
    }

    @ParameterizedTest
    @EnumSource(
            value = DeviceState.class,
            names = {"AVAILABLE", "INACTIVE"}
    )
    @DisplayName("update() should update fields when device is not IN_USE")
    void update_shouldModifyFieldsWhenAvailable(DeviceState state) {
        var id = 11L;
        var existing = Device.builder().id(id).name("Old name").brand("Old brand").state(state).build();
        var request = new DeviceUpdateRequest("New name", "New brand", state);
        var expectedResponse = new DeviceResponse(id, "New name", "New brand", state, now());
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(mapper.toResponse(existing)).thenReturn(expectedResponse);

        var result = service.update(id, request);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(existing.getName()).isEqualTo("New name");
        assertThat(existing.getBrand()).isEqualTo("New brand");
        assertThat(existing.getState()).isEqualTo(state);
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
    @DisplayName("patch() should update all provided fields when device is not IN_USE")
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
    @DisplayName("patch() should update only fields that are non-null in request")
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
    @DisplayName("patch() should throw when device is IN_USE and name or brand is changed")
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
    @DisplayName("patch() should allow changing state when device is IN_USE but name and brand do not change")
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
    @DisplayName("patch() should throw when device does not exist with given id")
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
    @DisplayName("getAll() should filter by given brand and return devices")
    void getAll_shouldFilterByBrand_whenOnlyBrandIsAvailable() {
        var apple1 = Device.builder().id(1L).name("IPhone").brand("Apple").build();
        var apple2 = Device.builder().id(2L).name("Macbook").brand("Apple").build();
        var response1 = new DeviceResponse(1L, "IPhone", "Apple", null, now());
        var response2 = new DeviceResponse(2L, "MacBook", "Apple", null, now());
        when(repository.findByBrandIgnoreCase("Apple")).thenReturn(List.of(apple1, apple2));
        when(mapper.toResponse(apple1)).thenReturn(response1);
        when(mapper.toResponse(apple2)).thenReturn(response2);

        var result = service.getAll("Apple",null );

        assertThat(result).containsExactly(response1, response2);
        verify(repository).findByBrandIgnoreCase("Apple");
        verify(mapper).toResponse(apple1);
        verify(mapper).toResponse(apple2);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    @DisplayName("getAll() should filter by given brand ignoring case and return devices")
    void getAll_shouldFilterByBrandIgnoringCase_whenOnlyBrandIsAvailable() {
        var apple1 = Device.builder().id(1L).name("IPhone").brand("Apple").build();
        var apple2 = Device.builder().id(2L).name("Macbook").brand("Apple").build();
        var response1 = new DeviceResponse(1L, "IPhone", "Apple", null, now());
        var response2 = new DeviceResponse(2L, "MacBook", "Apple", null, now());
        when(repository.findByBrandIgnoreCase("apple")).thenReturn(List.of(apple1, apple2));
        when(mapper.toResponse(apple1)).thenReturn(response1);
        when(mapper.toResponse(apple2)).thenReturn(response2);

        var result = service.getAll("apple",null );

        assertThat(result).containsExactly(response1, response2);
        verify(repository).findByBrandIgnoreCase("apple");
        verify(mapper).toResponse(apple1);
        verify(mapper).toResponse(apple2);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    @DisplayName("getAll() should filter by given state and return devices")
    void getAll_shouldFilterByState_whenOnlyStateIsAvailable() {
        var device1 = Device.builder().id(1L).name("IPhone").brand("Apple").state(AVAILABLE).build();
        var device2 = Device.builder().id(2L).name("Macbook").brand("Apple").state(AVAILABLE).build();
        var response1 = new DeviceResponse(1L, "IPhone", "Apple", AVAILABLE, now());
        var response2 = new DeviceResponse(2L, "MacBook", "Apple", AVAILABLE, now());
        when(repository.findByState(AVAILABLE)).thenReturn(List.of(device1, device2));
        when(mapper.toResponse(device1)).thenReturn(response1);
        when(mapper.toResponse(device2)).thenReturn(response2);

        var result = service.getAll(null, AVAILABLE);

        assertThat(result).containsExactly(response1, response2);
        verify(repository).findByState(AVAILABLE);
        verify(mapper).toResponse(device1);
        verify(mapper).toResponse(device2);
        verifyNoMoreInteractions(repository, mapper);
    }


    @Test
    @DisplayName("getAll() should return empty list if none of devices match the filters")
    void getAll_shouldReturnEmptyList_whenNoDevicesFoundWithGivenBrandAndState() {
        var device1 = Device.builder().id(1L).name("IPhone").brand("Apple").state(AVAILABLE).build();
        var device2 = Device.builder().id(2L).name("Galaxy S24").brand("Samsung").state(IN_USE).build();
        when(repository.findAll()).thenReturn(List.of(device1, device2));

        var result = service.getAll("Samsung", AVAILABLE);

        assertThat(result).isEmpty();
        verify(repository).findAll();
        verifyNoInteractions(mapper); // mapper should never be called
    }

    @Test
    @DisplayName("getAll() should return devices matching both brand and state when both filters are provided")
    void getAll_shouldFilterByBrandAndState_whenBothProvided() {
        var appleAvailable = Device.builder().id(1L).name("IPhone").brand("Apple").state(AVAILABLE).build();
        var samsungAvailable = Device.builder().id(2L).name("Galaxy S24").brand("Samsung").state(AVAILABLE).build();
        var samsungInUse = Device.builder().id(3L).name("Galaxy Tab").brand("Samsung").state(IN_USE).build();
        var response = new DeviceResponse(2L, "Galaxy S24", "Samsung", AVAILABLE, now());
        when(repository.findAll()).thenReturn(List.of(appleAvailable, samsungAvailable, samsungInUse));
        when(mapper.toResponse(samsungAvailable)).thenReturn(response);

        var result = service.getAll("Samsung", AVAILABLE);

        assertThat(result).containsExactly(response);
        verify(repository).findAll();
        verify(mapper).toResponse(samsungAvailable);
        verifyNoMoreInteractions(repository, mapper);
    }


    @Test
    @DisplayName("getAll() should return all devices when brand and state are not provided")
    void getAll_shouldReturnAll_whenBrandAndStateAreNotProvided() {
        var apple = Device.builder().id(1L).name("IPhone").brand("Apple").build();
        var samsung = Device.builder().id(2L).name("Galaxy S24").brand("Samsung").build();
        var response1 = new DeviceResponse(1L, "IPhone", "Apple", null, now());
        var response2 = new DeviceResponse(2L, "Galaxy S24", "Samsung", null, now());
        when(repository.findAll()).thenReturn(List.of(apple, samsung));
        when(mapper.toResponse(apple)).thenReturn(response1);
        when(mapper.toResponse(samsung)).thenReturn(response2);

        var result = service.getAll(null, null);

        assertThat(result).containsExactly(response1, response2);
        verify(repository).findAll();
        verify(mapper).toResponse(apple);
        verify(mapper).toResponse(samsung);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    @DisplayName("getAll() should return empty list when no devices exist")
    void getAll_shouldReturnEmptyList_whenNoDevicesExist() {
        when(repository.findAll()).thenReturn(List.of());

        var result = service.getAll(null, null);

        assertThat(result).isEmpty();
        verify(repository).findAll();
        verifyNoInteractions(mapper); // mapper should never be called
    }
}