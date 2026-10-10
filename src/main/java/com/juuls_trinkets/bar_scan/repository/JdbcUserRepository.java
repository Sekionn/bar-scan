package com.juuls_trinkets.bar_scan.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.juuls_trinkets.bar_scan.exception.ResourceNotFoundException;
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
                "select * from users where username = :username and active = true",
                Map.of("username", username),
                new AppUserMapper()
        );
        return users.stream().findFirst();
    }

    @Override
    public List<AppUser> findAllByCompanyId(UUID companyId) {
        return template.query(
                """
                select *
                from users
                where company_id = :companyId
                  and active = true
                order by shop_id is null, shop_id, username
                """,
                Map.of("companyId", companyId.toString()),
                new AppUserMapper()
        );
    }

    @Override
    public List<AppUser> findAllByShopId(UUID shopId) {
        return template.query(
                """
                select *
                from users
                where shop_id = :shopId
                  and active = true
                order by username
                """,
                Map.of("shopId", shopId.toString()),
                new AppUserMapper()
        );
    }

    @Override
    public long countByCompanyId(UUID companyId) {
        Long count = template.queryForObject(
                "select count(*) from users where company_id = :companyId and active = true",
                Map.of("companyId", companyId.toString()),
                Long.class
        );
        return count == null ? 0 : count;
    }

    @Override
    public long countByUsernamePrefix(String usernamePrefix) {
        Long count = template.queryForObject(
                "select count(*) from users where username like :usernamePattern",
                Map.of("usernamePattern", usernamePrefix + "-user-%"),
                Long.class
        );
        return count == null ? 0 : count;
    }

    @Override
    public AppUser create(AppUser user) {
        String sql = """
                insert into users(id, username, password_hash, company_id, shop_id, role)
                values(:id, :username, :passwordHash, :companyId, :shopId, :role)
                """;
        template.update(sql, new MapSqlParameterSource()
                .addValue("id", user.getId().toString())
                .addValue("username", user.getUsername())
                .addValue("passwordHash", user.getPasswordHash())
                .addValue("companyId", user.getCompanyId().toString())
                .addValue("shopId", user.getShopId() == null ? null : user.getShopId().toString())
                .addValue("role", user.getRole()));
        return user;
    }

    @Override
    public AppUser updatePassword(UUID userId, String passwordHash) {
        int updated = template.update(
                """
                update users
                set password_hash = :passwordHash
                where id = :id
                  and active = true
                """,
                Map.of(
                        "id", userId.toString(),
                        "passwordHash", passwordHash
                )
        );
        if (updated == 0) {
            throw new ResourceNotFoundException("User not found");
        }
        return findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public void deactivate(UUID userId) {
        int updated = template.update(
                """
                update users
                set active = false
                where id = :id
                  and active = true
                """,
                Map.of("id", userId.toString())
        );
        if (updated == 0) {
            throw new ResourceNotFoundException("User not found");
        }
    }

    @Override
    public void deactivateByShopId(UUID shopId) {
        template.update(
                """
                update users
                set active = false
                where shop_id = :shopId
                  and active = true
                """,
                Map.of("shopId", shopId.toString())
        );
    }
}
