package com.company.devices.repository;

import com.company.devices.domain.Device;
import com.company.devices.domain.DeviceState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findByBrandIgnoreCase(String brand);

    List<Device> findByState(DeviceState state);

}
