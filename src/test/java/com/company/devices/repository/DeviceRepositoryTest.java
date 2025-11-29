package com.company.devices.repository;

import com.company.devices.domain.Device;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static com.company.devices.domain.DeviceState.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DeviceRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("devices_db")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop"); // Let Hibernate create/drop schema
    }

    @Autowired
    private DeviceRepository deviceRepository;

    @BeforeEach
    void setUp() {
        deviceRepository.deleteAll();

        Device apple1 = Device.builder().name("iPhone").brand("Apple").state(AVAILABLE).build();
        Device apple2 = Device.builder().name("MacBook").brand("Apple").state(AVAILABLE).build();
        Device apple3 = Device.builder().name("IPhone 5S").brand("Apple").state(INACTIVE).build();
        Device apple4 = Device.builder().name("MacBook Air").brand("Apple").state(AVAILABLE).build();
        Device samsung1 = Device.builder().name("Galaxy S24").brand("Samsung").state(AVAILABLE).build();
        Device samsung2 = Device.builder().name("Galaxy Tab").brand("Samsung").state(INACTIVE).build();

        deviceRepository.saveAll(List.of(apple1, apple2, apple3, apple4, samsung1, samsung2));
    }

    @Test
    @DisplayName("findByBrandIgnoreCase returns devices of exact brand")
    void findByBrandIgnoreCase_shouldReturnDevicesForBrand() {
        List<Device> result = deviceRepository.findByBrandIgnoreCase("Apple");

        assertThat(result)
                .hasSize(4)
                .extracting(Device::getName)
                .containsExactlyInAnyOrder("iPhone", "MacBook", "IPhone 5S", "MacBook Air");
    }

    @Test
    @DisplayName("findByBrandIgnoreCase is case-insensitive")
    void findByBrandIgnoreCase_shouldBeCaseInsensitive() {
        List<Device> result = deviceRepository.findByBrandIgnoreCase("apple");

        assertThat(result)
                .hasSize(4)
                .extracting(Device::getBrand)
                .allMatch(brand -> brand.equals("Apple")); // db stores original case
    }

    @Test
    @DisplayName("findByBrandIgnoreCase returns empty list when no devices found")
    void findByBrandIgnoreCase_shouldReturnEmptyListWhenNoMatch() {
        List<Device> result = deviceRepository.findByBrandIgnoreCase("Nokia");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByBrandIgnoreCase returns only matching brand, even with multiple brands present")
    void findByBrandIgnoreCase_shouldNotReturnOtherBrands() {
        List<Device> result = deviceRepository.findByBrandIgnoreCase("Samsung");

        assertThat(result)
                .hasSize(2)
                .allSatisfy(device -> assertThat(device.getBrand()).isEqualTo("Samsung"))
                .extracting(Device::getName)
                .containsExactlyInAnyOrder("Galaxy S24", "Galaxy Tab");
    }

    @Test
    @DisplayName("findByState returns devices of exact state")
    void findByState_shouldReturnDevicesForState() {
        List<Device> result = deviceRepository.findByState(AVAILABLE);

        assertThat(result)
                .hasSize(4)
                .extracting(Device::getName)
                .containsExactlyInAnyOrder("iPhone", "Galaxy S24", "MacBook Air", "MacBook");
    }

    @Test
    @DisplayName("findByState returns empty list when no devices found")
    void findByState_shouldReturnEmptyListWhenNoMatch() {
        List<Device> result = deviceRepository.findByState(IN_USE);

        assertThat(result).isEmpty();
    }
}
