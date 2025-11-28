package com.company.devices.api;

import com.company.devices.api.dto.DeviceCreateRequest;
import com.company.devices.api.dto.DevicePatchRequest;
import com.company.devices.api.dto.DeviceResponse;
import com.company.devices.api.dto.DeviceUpdateRequest;
import com.company.devices.service.DeviceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController implements DeviceApi {

    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody DeviceCreateRequest request) {
        var body = service.create(request);
        return ResponseEntity.status(CREATED).body(body);
    }

    @Override
    public ResponseEntity<DeviceResponse> getById(@PathVariable Long id) {
        var body = service.getById(id);
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<DeviceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DeviceUpdateRequest request
    ) {
        var body = service.update(id, request);
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<DeviceResponse> patch(
            @PathVariable Long id,
            @Valid @RequestBody DevicePatchRequest request
    ) {
        var body = service.patch(id, request);
        return ResponseEntity.ok(body);
    }

    @Override
    public ResponseEntity<List<DeviceResponse>> getByBrand(@NotNull @RequestParam("brand") String brand) {
        var devicesByBrand = service.getByBrand(brand);
        return ResponseEntity.ok(devicesByBrand);
    }
}
