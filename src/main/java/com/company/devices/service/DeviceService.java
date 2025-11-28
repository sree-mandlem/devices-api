package com.company.devices.service;

import com.company.devices.api.dto.DeviceCreateRequest;
import com.company.devices.api.dto.DeviceResponse;
import com.company.devices.api.dto.DeviceUpdateRequest;
import com.company.devices.domain.Device;
import com.company.devices.domain.DeviceState;
import com.company.devices.repository.DeviceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class DeviceService {

    private final DeviceRepository repository;
    private final DeviceMapper mapper;

    public DeviceService(DeviceRepository repository, DeviceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public DeviceResponse create(DeviceCreateRequest request) {
        log.info("Creating device: {}", request);
        var device = mapper.toEntity(request);
        var saved = repository.save(device);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DeviceResponse getById(Long id) {
        log.info("Retrieving device by id: {}", id);
        var device = findDeviceOrThrow(id);
        return mapper.toResponse(device);
    }

    public void delete(Long id) {
        log.info("Deleting device by id: {}", id);
        var device = findDeviceOrThrow(id);

        if (device.getState() == DeviceState.IN_USE) {
            log.error("Deletion failed. Cannot delete device that is currently IN_USE");
            throw new IllegalStateException("Deletion failed. Cannot delete device that is currently IN_USE");
        }

        repository.delete(device);
    }

    public DeviceResponse update(Long id, DeviceUpdateRequest request) {
        var device = findDeviceOrThrow(id);

        var newName = request.name();
        var newBrand = request.brand();

        if (device.getState() == DeviceState.IN_USE) {
            boolean nameChanged = !device.getName().equals(newName);
            boolean brandChanged = !device.getBrand().equals(newBrand);
            if (nameChanged || brandChanged) {
                log.error("Name and brand cannot be updated when device is IN_USE");
                throw new IllegalStateException("Name and brand cannot be updated when device is IN_USE");
            }
        }

        // The `device` is a JPA-managed entity. Combining @Transactional, JPA flushes any changes to the DB.
        device.setName(newName);
        device.setBrand(newBrand);
        device.setState(request.state());

        return mapper.toResponse(device);
    }

    private Device findDeviceOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Retrieval failed. Entity not found with id: " + id));
    }
}
