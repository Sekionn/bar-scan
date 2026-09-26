package com.juuls_trinkets.bar_scan.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    public List<Product> findAllByCompanyId(UUID companyId) {
        return template.query(
                "select * from products where company_id = :companyId order by counted_date desc",
                Map.of("companyId", companyId.toString()),
                new ProductMapper()
        );
    }

    @Override
    public List<Product> findAllByCompanyIdAndQuarter(UUID companyId, int year, int quarter) {
        int startMonth = ((quarter - 1) * 3) + 1;
        return template.query(
                """
                select *
                from products
                where company_id = :companyId
                  and counted_date >= :startDate
                  and counted_date < date_add(:startDate, interval 3 month)
                order by counted_date desc
                """,
                new MapSqlParameterSource()
                        .addValue("companyId", companyId.toString())
                        .addValue("startDate", "%d-%02d-01".formatted(year, startMonth)),
                new ProductMapper()
        );
    }

    @Override
    public boolean existsByShopIdAndBarcode(UUID shopId, String barcode) {
        Integer count = template.queryForObject(
                """
                select count(*)
                from products
                where shop_id = :shopId
                  and barcode = :barcode
                """,
                Map.of(
                        "shopId", shopId.toString(),
                        "barcode", barcode
                ),
                Integer.class
        );
        return count != null && count > 0;
    }

    @Override
    public Optional<Product> findByCompanyIdAndBarcode(UUID companyId, String barcode) {
        List<Product> products = template.query(
                """
                        select *
                        from products
                        where company_id = :companyId
                          and barcode = :barcode
                        """,
                Map.of(
                        "companyId", companyId.toString(),
                        "barcode", barcode
                ),
                new ProductMapper()
        );
        return products.stream().findFirst();
    }

    @Override
    public Optional<Product> findByShopIdAndBarcode(UUID shopId, String barcode) {
        List<Product> products = template.query(
                """
                select *
                from products
                where shop_id = :shopId
                  and barcode = :barcode
                """,
                Map.of(
                        "shopId", shopId.toString(),
                        "barcode", barcode
                ),
                new ProductMapper()
        );
        return products.stream().findFirst();
    }

    @Override
    public int create(Product product) {
        String sql = """
                insert into products(company_id, shop_id, product_id, barcode, shelf_of_origin, store_segment, amount_counted, counted_date)
                values(:companyId, :shopId, :productId, :barcode, :shelfOfOrigin, :storeSegment, :amountCounted, :countedDate)
                """;
        return template.update(sql, productParameters(product));
    }

    @Override
    public Product addToCountByCompanyAndBarcode(Product product) {
        String sql = """
                insert into products(company_id, shop_id, product_id, barcode, shelf_of_origin, store_segment, amount_counted, counted_date)
                values(:companyId, :shopId, :productId, :barcode, :shelfOfOrigin, :storeSegment, :amountCounted, :countedDate)
                on duplicate key update
                    shelf_of_origin = values(shelf_of_origin),
                    store_segment = values(store_segment),
                    amount_counted = amount_counted + values(amount_counted),
                    counted_date = values(counted_date)
                """;
        template.update(sql, productParameters(product));
        return findByCompanyIdAndBarcode(product.getCompanyId(), product.getBarcode())
                .orElseThrow(() -> new IllegalStateException("Product was not found after additive upsert"));
    }

    @Override
    public Product addToCountByShopAndBarcode(Product product) {
        String sql = """
                insert into products(company_id, shop_id, product_id, barcode, shelf_of_origin, store_segment, amount_counted, counted_date)
                values(:companyId, :shopId, :productId, :barcode, :shelfOfOrigin, :storeSegment, :amountCounted, :countedDate)
                on duplicate key update
                    shelf_of_origin = values(shelf_of_origin),
                    store_segment = values(store_segment),
                    amount_counted = amount_counted + values(amount_counted),
                    counted_date = values(counted_date)
                """;
        template.update(sql, productParameters(product));
        return findByShopIdAndBarcode(product.getShopId(), product.getBarcode())
                .orElseThrow(() -> new IllegalStateException("Product was not found after additive shop upsert"));
    }

    @Override
    public int update(Product product) {
        String sql = """
                update products
                set barcode = :barcode,
                    shelf_of_origin = :shelfOfOrigin,
                    store_segment = :storeSegment,
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
                .addValue("shopId", product.getShopId() == null ? null : product.getShopId().toString())
                .addValue("productId", product.getProductId().toString())
                .addValue("barcode", product.getBarcode())
                .addValue("shelfOfOrigin", product.getShelfOfOrigin())
                .addValue("storeSegment", product.getStoreSegment())
                .addValue("amountCounted", product.getAmountCounted())
                .addValue("countedDate", product.getCountedDate());
    }
}
