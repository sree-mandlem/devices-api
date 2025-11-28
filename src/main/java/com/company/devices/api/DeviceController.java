package com.company.devices.api;

import com.company.devices.api.dto.DeviceCreateRequest;
import com.company.devices.api.dto.DeviceResponse;
import com.company.devices.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceResponse create(@Valid @RequestBody DeviceCreateRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public DeviceResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
