package com.juuls_trinkets.bar_scan.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;

import com.juuls_trinkets.bar_scan.model.ScanningSessionProduct;

class ScanningSessionProductMapper implements RowMapper<ScanningSessionProduct> {

    @Override
    public ScanningSessionProduct mapRow(ResultSet result, int rowNumber) throws SQLException {
        return new ScanningSessionProduct(
                UUID.fromString(result.getString("scanning_session_id")),
                UUID.fromString(result.getString("message_id")),
                result.getString("barcode"),
                result.getInt("shelf_of_origin"),
                result.getString("store_segment"),
                result.getInt("amount_counted"),
                result.getTimestamp("counted_date").toLocalDateTime()
        );
    }
}
