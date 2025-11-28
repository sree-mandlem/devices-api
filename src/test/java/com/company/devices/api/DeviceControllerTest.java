package com.company.devices.api;

import com.company.devices.api.dto.DeviceCreateRequest;
import com.company.devices.api.dto.DeviceResponse;
import com.company.devices.domain.DeviceState;
import com.company.devices.service.DeviceService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;


import static java.time.Instant.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        String invalidJson = """
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
        long id = 10L;
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
    @DisplayName("GET /api/v1/devices/{id} should return 404 when service throws EntityNotFoundException")
    void getById_shouldReturnNotFound() throws Exception {
        long id = 99L;
        var message = "Retrieval failed. Entity not found with id: " + id;
        when(deviceService.getById(id))
                .thenThrow(new EntityNotFoundException(message));

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
        long id = 3L;

        mockMvc.perform(delete("/api/v1/devices/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/devices/{id} should return 400 when service throws IllegalStateException")
    void delete_shouldReturnBadRequestOnIllegalState() throws Exception {
        long id = 4L;
        String message = "Deletion failed. Cannot delete device that is currently IN_USE";
        doThrow(new IllegalStateException(message))
                .when(deviceService).delete(eq(id));

        mockMvc.perform(delete("/api/v1/devices/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", containsString("Cannot delete device")));
    }
}