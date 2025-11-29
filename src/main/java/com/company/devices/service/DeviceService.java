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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
            throw new InvalidDeviceOperationException("Cannot delete device that is currently IN_USE");
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
        var device = findDeviceOrThrow(id);

        var newName = request.name() != null ? request.name() : device.getName();
        var newBrand = request.brand() != null ? request.brand() : device.getBrand();

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

    public List<DeviceResponse> getAll(String brand, DeviceState state) {
        List<Device> devices;
        if (brand != null && state != null) {
            log.info("Retrieving devices by brand: {} and state:{}", brand, state);
            devices = repository.findAll().stream()
                    .filter(d -> d.getBrand().equalsIgnoreCase(brand)
                            && d.getState() == state)
                    .toList();
        } else if (brand != null) {
            log.info("Retrieving devices by brand: {}", brand);
            devices = repository.findByBrandIgnoreCase(brand);
        } else if (state != null) {
            log.info("Retrieving devices by state: {}", state);
            devices = repository.findByState(state);
        } else {
            log.info("Retrieving all devices");
            devices = repository.findAll();
        }

        return devices.stream().map(mapper::toResponse).toList();
    }

    private Device findDeviceOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException(id));
    }

    private void enforceNameAndBrandUpdateRules(Device device, String newName, String newBrand) {
        if (device.getState() == DeviceState.IN_USE) {
            boolean nameChanged = !device.getName().equals(newName);
            boolean brandChanged = !device.getBrand().equals(newBrand);
            if (nameChanged || brandChanged) {
                throw new InvalidDeviceOperationException("Name and brand cannot be updated when device is IN_USE");
            }
        }
    }

}
