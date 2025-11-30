package com.company.devices.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class FlywayMigrationSmokeTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("devices_db")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // ✅ Rely on Flyway to create the schema; just validate with Hibernate
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

        // ✅ Make sure Flyway is enabled and uses the same location as in main config
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    }

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Flyway migrations are applied and schema is as expected")
    void flywayMigrationsShouldBeApplied() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT COUNT(*) FROM flyway_schema_history"
             );
             ResultSet rs = ps.executeQuery()) {

            assertThat(rs.next()).isTrue();
            int migrationCount = rs.getInt(1);
            assertThat(migrationCount)
                    .as("Expected at least one Flyway migration to be applied")
                    .isGreaterThan(0);
        }


        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT 1 FROM devices LIMIT 1"
             )) {
            ps.executeQuery(); // To the table exists and the query is valid.
        }
    }
}
