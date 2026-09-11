package com.juuls_trinkets.bar_scan.repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.juuls_trinkets.bar_scan.mapper.ProductMapper;
import com.juuls_trinkets.bar_scan.model.Product;

@Repository
public class JdbcProductRepository implements ProductRepository {

    private final NamedParameterJdbcTemplate template;

    public JdbcProductRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public List<Product> findAllByCompanyId(java.util.UUID companyId) {
        return template.query(
                "select * from products where company_id = :companyId order by counted_date desc",
                Map.of("companyId", companyId.toString()),
                new ProductMapper()
        );
    }

    @Override
    public int create(Product product) {
        String sql = """
                insert into products(company_id, product_id, barcode, shelf_of_origin, amount_counted, counted_date)
                values(:companyId, :productId, :barcode, :shelfOfOrigin, :amountCounted, :countedDate)
                """;
        return template.update(sql, productParameters(product));
    }

    @Override
    public int update(Product product) {
        String sql = """
                update products
                set barcode = :barcode,
                    shelf_of_origin = :shelfOfOrigin,
                    amount_counted = :amountCounted,
                    counted_date = :countedDate
                where company_id = :companyId
                  and product_id = :productId
                """;
        return template.update(sql, productParameters(product));
    }

    @Override
    public void delete(Product product) {
        template.update(
                "delete from products where company_id = :companyId and product_id = :productId",
                Map.of(
                        "companyId", product.getCompanyId().toString(),
                        "productId", product.getProductId().toString()
                )
        );
    }

    private SqlParameterSource productParameters(Product product) {
        return new MapSqlParameterSource()
                .addValue("companyId", product.getCompanyId().toString())
                .addValue("productId", product.getProductId().toString())
                .addValue("barcode", product.getBarcode())
                .addValue("shelfOfOrigin", product.getShelfOfOrigin())
                .addValue("amountCounted", product.getAmountCounted())
                .addValue("countedDate", product.getCountedDate());
    }
}
