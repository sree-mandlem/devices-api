package com.company.devices.api;

import com.company.devices.api.dto.DeviceCreateRequest;
import com.company.devices.api.dto.DevicePatchRequest;
import com.company.devices.api.dto.DeviceResponse;
import com.company.devices.api.dto.DeviceUpdateRequest;
import com.company.devices.domain.DeviceState;
import com.company.devices.domain.exception.DeviceNotFoundException;
import com.company.devices.domain.exception.InvalidDeviceOperationException;
import com.company.devices.service.DeviceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

import static java.time.Instant.now;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DeviceController.class)
@Import(GlobalExceptionHandler.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeviceService deviceService;

    @Test
    @DisplayName("POST /api/v1/devices should create a device and return 201 with body")
    void create_shouldReturnCreatedDevice() throws Exception {
        var request = new DeviceCreateRequest("Pixel", "Google", DeviceState.AVAILABLE);
        var response = new DeviceResponse(1L, "Pixel", "Google", DeviceState.AVAILABLE, now());
        when(deviceService.create(any(DeviceCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Pixel")))
                .andExpect(jsonPath("$.brand", is("Google")))
                .andExpect(jsonPath("$.state", is("AVAILABLE")));
    }

    @Test
    @DisplayName("POST /api/v1/devices should return 400 when device name is blank")
    void create_shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        var invalidJson = """
                {
                  "name": " ",
                  "brand": "Google",
                  "state": "AVAILABLE"
                }
                """;

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/devices/{id} should return 200 with device body")
    void getById_shouldReturnDevice() throws Exception {
        var id = 10L;
        var response = new DeviceResponse(id, "Kindle", "Amazon", DeviceState.AVAILABLE, now());
        when(deviceService.getById(id))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/devices/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is((int) id)))
                .andExpect(jsonPath("$.name", is("Kindle")))
                .andExpect(jsonPath("$.brand", is("Amazon")))
                .andExpect(jsonPath("$.state", is("AVAILABLE")));
    }

    @Test
    @DisplayName("GET /api/v1/devices/{id} should return 404 when device is not found")
    void getById_shouldReturnNotFound() throws Exception {
        var id = 99L;
        var message = "Device not found with id: " + id;
        when(deviceService.getById(id))
                .thenThrow(new DeviceNotFoundException(id));

        mockMvc.perform(get("/api/v1/devices/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is(message)));
    }

    @Test
    @DisplayName("DELETE /api/v1/devices/{id} should return 204 when deletion succeeds")
    void delete_shouldReturnNoContent() throws Exception {
        var id = 3L;

        mockMvc.perform(delete("/api/v1/devices/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/devices/{id} should return 400 when service throws InvalidDeviceOperationException")
    void delete_shouldReturnBadRequestOnIllegalState() throws Exception {
        var id = 4L;
        var message = "Deletion failed. Cannot delete device that is currently IN_USE";
        doThrow(new InvalidDeviceOperationException(message))
                .when(deviceService).delete(eq(id));

        mockMvc.perform(delete("/api/v1/devices/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Cannot delete device")));
    }

    @Test
    @DisplayName("PUT /api/v1/devices/{id} should return 200 with updated device")
    void update_shouldReturnUpdatedDevice() throws Exception {
        var id = 50L;
        var request = new DeviceUpdateRequest("Updated", "BrandX", DeviceState.AVAILABLE);
        var response = new DeviceResponse(id, "Updated", "BrandX", DeviceState.AVAILABLE, now());
        when(deviceService.update(eq(id), any(DeviceUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is((int) id)))
                .andExpect(jsonPath("$.name", is("Updated")))
                .andExpect(jsonPath("$.brand", is("BrandX")))
                .andExpect(jsonPath("$.state", is("AVAILABLE")));
    }

    @Test
    @DisplayName("PUT /api/v1/devices/{id} should return 400 when validation fails")
    void update_shouldReturnBadRequestOnValidationError() throws Exception {
        var invalidJson = """
                    {
                      "name": " ",
                      "brand": "BrandX",
                      "state": "AVAILABLE"
                    }
                    """;

        mockMvc.perform(put("/api/v1/devices/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/devices/{id} should return 404 when device is not found")
    void update_shouldReturnNotFound() throws Exception {
        var id = 404L;
        var request = new DeviceUpdateRequest("Updated", "BrandX", DeviceState.AVAILABLE);
        var message = "Device not found with id: " + id;
        when(deviceService.update(eq(id), any(DeviceUpdateRequest.class)))
                .thenThrow(new DeviceNotFoundException(id));

        mockMvc.perform(put("/api/v1/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is(message)));
    }

    @Test
    @DisplayName("PUT /api/v1/devices/{id} should return 400 when service rejects name/brand change for IN_USE")
    void update_shouldReturnBadRequestWhenBusinessRuleViolated() throws Exception {
        var id = 400L;
        var request = new DeviceUpdateRequest("New name", "BrandX", DeviceState.IN_USE);
        var message = "Name and brand cannot be updated when device is IN_USE";
        when(deviceService.update(eq(id), any(DeviceUpdateRequest.class)))
                .thenThrow(new InvalidDeviceOperationException(message));

        mockMvc.perform(put("/api/v1/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is(message)));
    }

    @Test
    @DisplayName("PATCH /api/v1/devices/{id} should return 200 with the patched device")
    void patch_shouldReturnUpdatedDevice() throws Exception {
        var id = 50L;
        var request = new DevicePatchRequest("Updated", "BrandX", DeviceState.AVAILABLE);
        var response = new DeviceResponse(id, "Updated", "BrandX", DeviceState.AVAILABLE, now());
        when(deviceService.patch(eq(id), any(DevicePatchRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is((int) id)))
                .andExpect(jsonPath("$.name", is("Updated")))
                .andExpect(jsonPath("$.brand", is("BrandX")))
                .andExpect(jsonPath("$.state", is("AVAILABLE")));
    }

    @Test
    @DisplayName("PATCH /api/v1/devices/{id} should return 404 when device is not found")
    void patch_shouldReturnNotFound() throws Exception {
        var id = 404L;
        var request = new DevicePatchRequest("Updated", "BrandX", DeviceState.AVAILABLE);
        var message = "Device not found with id: " + id;
        when(deviceService.patch(eq(id), any(DevicePatchRequest.class)))
                .thenThrow(new DeviceNotFoundException(id));

        mockMvc.perform(patch("/api/v1/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is(message)));
    }

    @Test
    @DisplayName("PATCH /api/v1/devices/{id} should return 400 when service rejects name/brand change for IN_USE")
    void patch_shouldReturnBadRequestWhenBusinessRuleViolated() throws Exception {
        var id = 400L;
        var request = new DevicePatchRequest("New name", "BrandX", DeviceState.IN_USE);
        var message = "Name and brand cannot be updated when device is IN_USE";
        when(deviceService.patch(eq(id), any(DevicePatchRequest.class)))
                .thenThrow(new InvalidDeviceOperationException(message));

        mockMvc.perform(patch("/api/v1/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is(message)));
    }

    @Test
    @DisplayName("GET /api/v1/devices should return devices filtered by brand and state")
    void getAll_shouldReturnDevicesFilteredByBrandAndState() throws Exception {
        var response1 = new DeviceResponse(1L, "IPhone", "Apple", DeviceState.AVAILABLE, now());
        var response2 = new DeviceResponse(2L, "MacBook", "Apple", DeviceState.AVAILABLE, now());

        when(deviceService.getAll("Apple", DeviceState.AVAILABLE))
                .thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/v1/devices")
                        .param("brand", "Apple")
                        .param("state", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("IPhone")))
                .andExpect(jsonPath("$[0].brand", is("Apple")))
                .andExpect(jsonPath("$[0].state", is("AVAILABLE")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("MacBook")))
                .andExpect(jsonPath("$[1].brand", is("Apple")))
                .andExpect(jsonPath("$[1].state", is("AVAILABLE")));

        verify(deviceService).getAll("Apple", DeviceState.AVAILABLE);
        verifyNoMoreInteractions(deviceService);
    }

    @Test
    @DisplayName("GET /api/v1/devices should return devices matching given brand when only provided")
    void getAll_shouldReturnDevicesMatchingBrandWhenOnlyBrandIsProvided() throws Exception {
        var response1 = new DeviceResponse(1L, "IPhone", "Apple", DeviceState.AVAILABLE, Instant.now());
        var response2 = new DeviceResponse(2L, "MacBook", "Apple", DeviceState.AVAILABLE, Instant.now());

        when(deviceService.getAll("Apple", null))
                .thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/v1/devices")
                        .param("brand", "Apple"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // list size
                .andExpect(jsonPath("$", hasSize(2)))
                // first device
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("IPhone")))
                .andExpect(jsonPath("$[0].brand", is("Apple")))
                .andExpect(jsonPath("$[0].state", is("AVAILABLE")))
                // second device
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("MacBook")))
                .andExpect(jsonPath("$[1].brand", is("Apple")))
                .andExpect(jsonPath("$[1].state", is("AVAILABLE")));
        verify(deviceService).getAll("Apple", null);
        verifyNoMoreInteractions(deviceService);
    }

    @Test
    @DisplayName("GET /api/v1/devices should return devices matching given brand case insensitive when only provided")
    void getAll_shouldReturnDevicesMatchingBrandCaseInsensitiveWhenOnlyBrandIsProvided() throws Exception {
        // API receives lowercase "apple" but service/repo is case-insensitive
        var response1 = new DeviceResponse(1L, "IPhone", "Apple", DeviceState.AVAILABLE, Instant.now());
        var response2 = new DeviceResponse(2L, "MacBook", "Apple", DeviceState.AVAILABLE, Instant.now());

        when(deviceService.getAll("apple", null))
                .thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/v1/devices")
                        .param("brand", "apple"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].brand", is("Apple")))
                .andExpect(jsonPath("$[1].brand", is("Apple")));
        verify(deviceService).getAll("apple", null);
        verifyNoMoreInteractions(deviceService);
    }

    @Test
    @DisplayName("GET /api/v1/devices should return devices matching given state when only provided")
    void getAll_shouldReturnDevicesMatchingStateWhenOnlyStateIsProvided() throws Exception {
        var response1 = new DeviceResponse(1L, "IPhone", "Apple", DeviceState.AVAILABLE, Instant.now());
        var response2 = new DeviceResponse(2L, "Galaxy S24", "Samsung", DeviceState.AVAILABLE, Instant.now());

        when(deviceService.getAll(null, DeviceState.AVAILABLE))
                .thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/v1/devices")
                        .param("state", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                // both have the requested state
                .andExpect(jsonPath("$[0].state", is("AVAILABLE")))
                .andExpect(jsonPath("$[1].state", is("AVAILABLE")));
        verify(deviceService).getAll(null, DeviceState.AVAILABLE);
        verifyNoMoreInteractions(deviceService);
    }

    @Test
    @DisplayName("GET /api/v1/devices should return all devices when no filters are provided")
    void getAll_shouldReturnAllDevicesWhenNoFilters() throws Exception {
        var response1 = new DeviceResponse(1L, "IPhone", "Apple", null, now());
        var response2 = new DeviceResponse(2L, "Galaxy S24", "Samsung", null, now());

        when(deviceService.getAll(null, null))
                .thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/v1/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("IPhone")))
                .andExpect(jsonPath("$[0].brand", is("Apple")))
                .andExpect(jsonPath("$[0].state").doesNotExist())
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Galaxy S24")))
                .andExpect(jsonPath("$[1].brand", is("Samsung")))
                .andExpect(jsonPath("$[1].state").doesNotExist());

        verify(deviceService).getAll(null, null);
        verifyNoMoreInteractions(deviceService);
    }

    @Test
    @DisplayName("GET /api/v1/devices should return 400 Bad Request when state enum value is invalid")
    void getAll_shouldReturnBadRequestOnInvalidEnumValue() throws Exception {
        mockMvc.perform(get("/api/v1/devices")
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/devices should return 500 when service throws an unexpected exception")
    void getAll_shouldReturnInternalServerErrorWhenServiceFails() throws Exception {
        when(deviceService.getAll("Apple", null))
                .thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(get("/api/v1/devices").param("brand", "Apple"))
                .andExpect(status().isInternalServerError());

        verify(deviceService).getAll("Apple", null);
        verifyNoMoreInteractions(deviceService);
    }
}