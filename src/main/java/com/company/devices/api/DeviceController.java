package com.company.devices.api;

import com.company.devices.api.dto.DeviceCreateRequest;
import com.company.devices.api.dto.DeviceResponse;
import com.company.devices.api.dto.DeviceUpdateRequest;
import com.company.devices.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        DeviceResponse body = service.create(request);
        return ResponseEntity.status(CREATED).body(body);
    }

    @Override
    public ResponseEntity<DeviceResponse> getById(@PathVariable Long id) {
        DeviceResponse body = service.getById(id);
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
        DeviceResponse body = service.update(id, request);
        return ResponseEntity.ok(body);
    }
}
