package com.company.devices.api;

import com.company.devices.api.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/devices")
public interface DeviceApi {

    @Operation(summary = "Create a new device")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device created successfully",
                    content = @Content(schema = @Schema(implementation = DeviceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    ResponseEntity<DeviceResponse> create(
            @Valid
            @RequestBody(description = "Payload to create a new device", required = true)
            @org.springframework.web.bind.annotation.RequestBody DeviceCreateRequest request
    );

    @Operation(summary = "Get a device by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DeviceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    ResponseEntity<DeviceResponse> getById(@PathVariable Long id);

    @Operation(summary = "Delete a device by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Device deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Deletion not allowed for current device state",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id);

    @Operation(summary = "Update an existing device, but only when not in use.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device updated successfully",
                    content = @Content(schema = @Schema(implementation = DeviceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    ResponseEntity<DeviceResponse> update(
            @PathVariable Long id,
            @RequestBody(description = "Payload to patch an existing device", required = true)
            @Valid @org.springframework.web.bind.annotation.RequestBody DeviceUpdateRequest request
    );

    @Operation(summary = "Patch update an existing device, but only when not in use.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Device patched successfully",
                    content = @Content(schema = @Schema(implementation = DeviceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}")
    ResponseEntity<DeviceResponse> patch(
            @PathVariable Long id,
            @RequestBody(description = "Payload to update an existing device", required = true)
            @Valid @org.springframework.web.bind.annotation.RequestBody DevicePatchRequest request
    );

    @Operation(summary = "Fetch devices filtered by brand")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DeviceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    ResponseEntity<List<DeviceResponse>> getByBrand(@NotNull @RequestParam("brand") String brand);
}
