package com.juuls_trinkets.bar_scan.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.juuls_trinkets.bar_scan.exception.DuplicateResourceException;
import com.juuls_trinkets.bar_scan.model.ScanningSession;
import com.juuls_trinkets.bar_scan.model.ScanningSessionProduct;

@Repository
public class JdbcScanningSessionRepository implements ScanningSessionRepository {

    private final NamedParameterJdbcTemplate template;

    public JdbcScanningSessionRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public ScanningSession create(ScanningSession session) {
        try {
            template.update(
                    """
                    insert into scanning_sessions(id, company_id, shop_id, started_by_user_id, started_at, closed_at)
                    values(:id, :companyId, :shopId, :startedByUserId, :startedAt, :closedAt)
                    """,
                    sessionParameters(session)
            );
        } catch (DuplicateKeyException ex) {
            throw new DuplicateResourceException("A scanning session is already open for this shop");
        }
        return session;
    }

    @Override
    public Optional<ScanningSession> findOpenByShopId(UUID shopId) {
        return template.query(
                """
                select *
                from scanning_sessions
                where shop_id = :shopId
                  and closed_at is null
                order by started_at desc
                limit 1
                """,
                Map.of("shopId", shopId.toString()),
                new ScanningSessionMapper()
        ).stream().findFirst();
    }

    @Override
    public ScanningSessionProduct addProductScan(ScanningSessionProduct product) {
        try {
            template.update(
                """
                insert into scanning_session_products(
                    scanning_session_id,
                    message_id,
                    barcode,
                    shelf_of_origin,
                    store_segment,
                    amount_counted,
                    counted_date
                )
                values(
                    :scanningSessionId,
                    :messageId,
                    :barcode,
                    :shelfOfOrigin,
                    :storeSegment,
                    :amountCounted,
                    :countedDate
                )
                """,
                new MapSqlParameterSource()
                        .addValue("scanningSessionId", product.getScanningSessionId().toString())
                        .addValue("messageId", product.getMessageId().toString())
                        .addValue("barcode", product.getBarcode())
                        .addValue("shelfOfOrigin", product.getShelfOfOrigin())
                        .addValue("storeSegment", product.getStoreSegment())
                        .addValue("amountCounted", product.getAmountCounted())
                        .addValue("countedDate", product.getCountedDate())
            );
        } catch (DuplicateKeyException ignored) {
            return findProductByMessageId(product.getMessageId())
                    .orElseThrow(() -> new IllegalStateException("Session product was not found after duplicate scan insert"));
        }
        return findProductByMessageId(product.getMessageId())
                .orElseThrow(() -> new IllegalStateException("Session product was not found after scan insert"));
    }

    private Optional<ScanningSessionProduct> findProductByMessageId(UUID messageId) {
        return template.query(
                """
                select *
                from scanning_session_products
                where message_id = :messageId
                """,
                Map.of("messageId", messageId.toString()),
                new ScanningSessionProductMapper()
        ).stream().findFirst();
    }

    private MapSqlParameterSource sessionParameters(ScanningSession session) {
        return new MapSqlParameterSource()
                .addValue("id", session.getId().toString())
                .addValue("companyId", session.getCompanyId().toString())
                .addValue("shopId", session.getShopId().toString())
                .addValue("startedByUserId", session.getStartedByUserId().toString())
                .addValue("startedAt", session.getStartedAt())
                .addValue("closedAt", session.getClosedAt());
    }

    private static class ScanningSessionMapper implements RowMapper<ScanningSession> {

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
    }

    private static class ScanningSessionProductMapper implements RowMapper<ScanningSessionProduct> {

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

    private static LocalDateTime nullableLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
