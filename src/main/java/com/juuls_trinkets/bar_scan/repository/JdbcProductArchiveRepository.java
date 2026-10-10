package com.juuls_trinkets.bar_scan.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.juuls_trinkets.bar_scan.dto.ProductArchiveItemResponseDTO;
import com.juuls_trinkets.bar_scan.model.ProductArchiveSummaryRow;

@Repository
public class JdbcProductArchiveRepository implements ProductArchiveRepository {

    private final NamedParameterJdbcTemplate template;

    public JdbcProductArchiveRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public List<ProductArchiveSummaryRow> findSummary(UUID companyId, UUID shopId) {
        String shopFilter = shopId == null ? "" : " and s.shop_id = :shopId";
        MapSqlParameterSource parameters = baseParameters(companyId, shopId);
        return template.query(
                """
                select
                    s.shop_id,
                    sh.name as shop_name,
                    year(sp.counted_date) as counted_year,
                    quarter(sp.counted_date) as counted_quarter,
                    date(sp.counted_date) as counted_day,
                    count(*) as product_count
                from scanning_session_products sp
                join scanning_sessions s on s.id = sp.scanning_session_id
                join shops sh on sh.id = s.shop_id
                where s.company_id = :companyId
                  and sh.active = true
                """ + shopFilter + """

                group by s.shop_id, sh.name, counted_year, counted_quarter, counted_day
                order by sh.name, counted_year desc, counted_quarter, counted_day desc
                """,
                parameters,
                (result, rowNumber) -> new ProductArchiveSummaryRow(
                        UUID.fromString(result.getString("shop_id")),
                        result.getString("shop_name"),
                        result.getInt("counted_year"),
                        result.getInt("counted_quarter"),
                        result.getDate("counted_day").toLocalDate(),
                        result.getLong("product_count")
                )
        );
    }

    @Override
    public long countProducts(UUID companyId, UUID shopId, Integer year, String quarter, LocalDate date) {
        Long count = template.queryForObject(
                """
                select count(*)
                from (
                    select 1
                    from scanning_session_products sp
                    join scanning_sessions s on s.id = sp.scanning_session_id
                    join shops sh on sh.id = s.shop_id
                    where
                """ + filterClause() + """

                    group by s.shop_id, sh.name, sp.barcode, sp.shelf_of_origin, sp.store_segment, date(sp.counted_date)
                ) grouped_products
                """,
                filterParameters(companyId, shopId, year, quarter, date),
                Long.class
        );
        return count == null ? 0 : count;
    }

    @Override
    public List<ProductArchiveItemResponseDTO> findProducts(
            UUID companyId,
            UUID shopId,
            Integer year,
            String quarter,
            LocalDate date,
            int limit,
            int offset
    ) {
        MapSqlParameterSource parameters = filterParameters(companyId, shopId, year, quarter, date)
                .addValue("limit", limit)
                .addValue("offset", offset);
        return template.query(
                productsQuery("limit :limit offset :offset"),
                parameters,
                this::mapArchiveItem
        );
    }

    @Override
    public List<ProductArchiveItemResponseDTO> findProductsForExport(
            UUID companyId,
            UUID shopId,
            Integer year,
            String quarter,
            LocalDate date
    ) {
        return template.query(
                productsQuery(""),
                filterParameters(companyId, shopId, year, quarter, date),
                this::mapArchiveItem
        );
    }

    private String productsQuery(String suffix) {
        return """
                select
                    s.shop_id,
                    sh.name as shop_name,
                    min(sp.scanning_session_id) as scanning_session_id,
                    sp.barcode,
                    sp.shelf_of_origin,
                    sp.store_segment,
                    sum(sp.amount_counted) as amount_counted,
                    max(sp.counted_date) as counted_date,
                    year(sp.counted_date) as counted_year,
                    quarter(sp.counted_date) as counted_quarter,
                    date(sp.counted_date) as counted_day
                from scanning_session_products sp
                join scanning_sessions s on s.id = sp.scanning_session_id
                join shops sh on sh.id = s.shop_id
                where
                """ + filterClause() + """

                group by s.shop_id, sh.name, sp.barcode, sp.shelf_of_origin, sp.store_segment, counted_year, counted_quarter, counted_day
                order by counted_day desc, sp.barcode, sp.store_segment
                """ + suffix + """
                """;
    }

    private ProductArchiveItemResponseDTO mapArchiveItem(java.sql.ResultSet result, int rowNumber) throws java.sql.SQLException {
        return new ProductArchiveItemResponseDTO(
                UUID.fromString(result.getString("shop_id")),
                result.getString("shop_name"),
                UUID.fromString(result.getString("scanning_session_id")),
                result.getString("barcode"),
                result.getInt("shelf_of_origin"),
                result.getString("store_segment"),
                result.getInt("amount_counted"),
                result.getTimestamp("counted_date").toLocalDateTime(),
                result.getInt("counted_year"),
                "Q" + result.getInt("counted_quarter"),
                result.getDate("counted_day").toLocalDate()
        );
    }

    private String filterClause() {
        return """
                s.company_id = :companyId
                  and s.shop_id = :shopId
                  and sh.active = true
                  and (
                    (:date is not null and date(sp.counted_date) = :date)
                    or (
                      :date is null
                      and :year is not null
                      and :quarterNumber is not null
                      and year(sp.counted_date) = :year
                      and quarter(sp.counted_date) = :quarterNumber
                    )
                  )
                """;
    }

    private MapSqlParameterSource baseParameters(UUID companyId, UUID shopId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("companyId", companyId.toString());
        if (shopId != null) {
            parameters.addValue("shopId", shopId.toString());
        }
        return parameters;
    }

    private MapSqlParameterSource filterParameters(UUID companyId, UUID shopId, Integer year, String quarter, LocalDate date) {
        return new MapSqlParameterSource()
                .addValue("companyId", companyId.toString())
                .addValue("shopId", shopId.toString())
                .addValue("year", year)
                .addValue("quarterNumber", quarterNumber(quarter))
                .addValue("date", date == null ? null : Date.valueOf(date));
    }

    private Integer quarterNumber(String quarter) {
        if (quarter == null || quarter.isBlank()) {
            return null;
        }
        return Integer.parseInt(quarter.replace("Q", ""));
    }
}
