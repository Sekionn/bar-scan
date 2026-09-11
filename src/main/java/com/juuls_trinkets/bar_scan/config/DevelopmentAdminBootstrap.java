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
public class DevelopmentAdminBootstrap implements ApplicationRunner {

    private final NamedParameterJdbcTemplate template;
    private final PasswordEncoder passwordEncoder;
    private final UUID companyId;
    private final UUID adminId;
    private final String username;
    private final String password;

    public DevelopmentAdminBootstrap(
            NamedParameterJdbcTemplate template,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.company-id}") UUID companyId,
            @Value("${app.bootstrap-admin.user-id}") UUID adminId,
            @Value("${app.bootstrap-admin.username}") String username,
            @Value("${app.bootstrap-admin.password}") String password
    ) {
        this.template = template;
        this.passwordEncoder = passwordEncoder;
        this.companyId = companyId;
        this.adminId = adminId;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        template.update(
                """
                INSERT INTO companies(id, name, allowed_user_count, location_name)
                VALUES (:companyId, 'Development Company', 10, 'Development Store')
                ON DUPLICATE KEY UPDATE
                    name = VALUES(name),
                    allowed_user_count = VALUES(allowed_user_count),
                    location_name = VALUES(location_name)
                """,
                Map.of("companyId", companyId.toString())
        );

        template.update(
                """
                INSERT INTO users(id, username, password_hash, company_id, role)
                VALUES (:adminId, :username, :passwordHash, :companyId, 'ROLE_ADMIN')
                ON DUPLICATE KEY UPDATE
                    password_hash = VALUES(password_hash),
                    company_id = VALUES(company_id),
                    role = VALUES(role)
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
