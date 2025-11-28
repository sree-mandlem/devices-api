package com.company.devices.service;

import com.company.devices.api.dto.*;
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
            log.warn("Cannot delete device that is currently IN_USE");
            throw new IllegalStateException("Cannot delete device that is currently IN_USE");
        }

        repository.delete(device);
    }

    public DeviceResponse update(Long id, DeviceUpdateRequest request) {
        log.info("Updating device by id: {}, request:{}", id, request);
        var device = findDeviceOrThrow(id);

        var newName = request.name();
        var newBrand = request.brand();

        enforceNameAndBrandUpdateRules(device, newName, newBrand);

        // The `device` is a JPA-managed entity. Combining @Transactional, JPA flushes any changes to the DB.
        device.setName(newName);
        device.setBrand(newBrand);
        device.setState(request.state());

        return mapper.toResponse(device);
    }

    public DeviceResponse patch(Long id, DevicePatchRequest request) {
        log.info("Patching device by id: {}, request:{}", id, request);
        Device device = findDeviceOrThrow(id);

        String newName = request.name() != null ? request.name() : device.getName();
        String newBrand = request.brand() != null ? request.brand() : device.getBrand();

        enforceNameAndBrandUpdateRules(device, newName, newBrand);

        if (request.name() != null) {
            device.setName(request.name());
        }
        if (request.brand() != null) {
            device.setBrand(request.brand());
        }
        if (request.state() != null) {
            device.setState(request.state());
        }

        return mapper.toResponse(device);
    }

    private Device findDeviceOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device not found with id: " + id));
    }

    private void enforceNameAndBrandUpdateRules(Device device, String newName, String newBrand) {
        if (device.getState() == DeviceState.IN_USE) {
            boolean nameChanged = !device.getName().equals(newName);
            boolean brandChanged = !device.getBrand().equals(newBrand);
            if (nameChanged || brandChanged) {
                throw new IllegalStateException("Name and brand cannot be updated when device is IN_USE");
            }
        }
    }
}
