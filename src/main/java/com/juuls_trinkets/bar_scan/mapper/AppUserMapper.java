package com.juuls_trinkets.bar_scan.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;

import com.juuls_trinkets.bar_scan.model.AppUser;

public class AppUserMapper implements RowMapper<AppUser> {

    @Override
    public AppUser mapRow(ResultSet result, int rowNumber) throws SQLException {
        return new AppUser(
                UUID.fromString(result.getString("id")),
                result.getString("username"),
                result.getString("password_hash"),
                UUID.fromString(result.getString("company_id")),
                result.getString("role")
        );
    }
}
