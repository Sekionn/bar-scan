package com.juuls_trinkets.bar_scan;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import com.juuls_trinkets.bar_scan.models.Product;

public class ProductMapper implements RowMapper<Product> {

    @Override
    public Product mapRow(ResultSet result, int arg1) throws SQLException {
        Product product = new Product(
            result.getString("barcode"),
            result.getInt("shelfOfOrigin"),
            result.getInt("amountCounted"),
            result.getTimestamp("countedDate").toLocalDateTime(),
            result.getObject("id", java.util.UUID.class)
        );

        return product;
    }
}
