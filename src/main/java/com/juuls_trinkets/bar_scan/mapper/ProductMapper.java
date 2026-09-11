package com.juuls_trinkets.bar_scan.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;

import com.juuls_trinkets.bar_scan.model.Product;

public class ProductMapper implements RowMapper<Product> {

    @Override
    public Product mapRow(ResultSet result, int rowNumber) throws SQLException {
        return new Product(
                UUID.fromString(result.getString("company_id")),
                UUID.fromString(result.getString("product_id")),
                result.getString("barcode"),
                result.getInt("shelf_of_origin"),
                result.getInt("amount_counted"),
                result.getTimestamp("counted_date").toLocalDateTime()
        );
    }
}
