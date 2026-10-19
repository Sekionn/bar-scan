package com.juuls_trinkets.bar_scan.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;

import com.juuls_trinkets.bar_scan.model.ScanningSession;

class ScanningSessionMapper implements RowMapper<ScanningSession> {

    @Override
    public ScanningSession mapRow(ResultSet result, int rowNumber) throws SQLException {
        return new ScanningSession(
                UUID.fromString(result.getString("id")),
                UUID.fromString(result.getString("company_id")),
                UUID.fromString(result.getString("shop_id")),
                UUID.fromString(result.getString("started_by_user_id")),
                result.getTimestamp("started_at").toLocalDateTime(),
                nullableLocalDateTime(result.getTimestamp("closed_at"))
        );
    }

    private static LocalDateTime nullableLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
