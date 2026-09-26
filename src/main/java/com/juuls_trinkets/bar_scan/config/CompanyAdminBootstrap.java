package com.juuls_trinkets.bar_scan.config;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.bootstrap-admin.enabled", havingValue = "true")
public class CompanyAdminBootstrap implements ApplicationRunner {

    private final NamedParameterJdbcTemplate template;
    private final PasswordEncoder passwordEncoder;
    private final UUID companyId;
    private final String companyName;
    private final int allowedUserCount;
    private final String locationName;
    private final UUID adminId;
    private final String username;
    private final String password;

    public CompanyAdminBootstrap(
            NamedParameterJdbcTemplate template,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.company-id}") UUID companyId,
            @Value("${app.bootstrap-admin.company-name}") String companyName,
            @Value("${app.bootstrap-admin.allowed-user-count}") int allowedUserCount,
            @Value("${app.bootstrap-admin.location-name}") String locationName,
            @Value("${app.bootstrap-admin.user-id}") UUID adminId,
            @Value("${app.bootstrap-admin.username}") String username,
            @Value("${app.bootstrap-admin.password}") String password
    ) {
        this.template = template;
        this.passwordEncoder = passwordEncoder;
        this.companyId = companyId;
        this.companyName = companyName;
        this.allowedUserCount = allowedUserCount;
        this.locationName = locationName;
        this.adminId = adminId;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("APP_BOOTSTRAP_ADMIN_PASSWORD must be set when bootstrap admin is enabled");
        }

        template.update(
                """
                INSERT INTO companies(id, name, allowed_user_count, location_name)
                VALUES (:companyId, :companyName, :allowedUserCount, :locationName)
                ON DUPLICATE KEY UPDATE
                    name = :companyName,
                    allowed_user_count = :allowedUserCount,
                    location_name = :locationName
                """,
                Map.of(
                        "companyId", companyId.toString(),
                        "companyName", companyName,
                        "allowedUserCount", allowedUserCount,
                        "locationName", locationName
                )
        );

        template.update(
                """
                INSERT INTO users(id, username, password_hash, company_id, role)
                VALUES (:adminId, :username, :passwordHash, :companyId, 'ROLE_SUPER_ADMIN')
                ON DUPLICATE KEY UPDATE
                    username = :username,
                    password_hash = :passwordHash,
                    company_id = :companyId,
                    role = 'ROLE_SUPER_ADMIN'
                """,
                Map.of(
                        "adminId", adminId.toString(),
                        "username", username,
                        "passwordHash", passwordEncoder.encode(password),
                        "companyId", companyId.toString()
                )
        );
    }
}
