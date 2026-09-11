package com.juuls_trinkets.bar_scan.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.juuls_trinkets.bar_scan.mapper.AppUserMapper;
import com.juuls_trinkets.bar_scan.model.AppUser;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final NamedParameterJdbcTemplate template;

    public JdbcUserRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public Optional<AppUser> findById(UUID id) {
        List<AppUser> users = template.query(
                "select * from users where id = :id",
                Map.of("id", id.toString()),
                new AppUserMapper()
        );
        return users.stream().findFirst();
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        List<AppUser> users = template.query(
                "select * from users where username = :username",
                Map.of("username", username),
                new AppUserMapper()
        );
        return users.stream().findFirst();
    }

    @Override
    public long countByCompanyId(UUID companyId) {
        Long count = template.queryForObject(
                "select count(*) from users where company_id = :companyId",
                Map.of("companyId", companyId.toString()),
                Long.class
        );
        return count == null ? 0 : count;
    }

    @Override
    public AppUser create(AppUser user) {
        String sql = """
                insert into users(id, username, password_hash, company_id, role)
                values(:id, :username, :passwordHash, :companyId, :role)
                """;
        template.update(sql, Map.of(
                "id", user.getId().toString(),
                "username", user.getUsername(),
                "passwordHash", user.getPasswordHash(),
                "companyId", user.getCompanyId().toString(),
                "role", user.getRole()
        ));
        return user;
    }
}
