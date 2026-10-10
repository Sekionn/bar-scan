package com.juuls_trinkets.bar_scan.repository;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcScanDataRetentionRepository implements ScanDataRetentionRepository {

    private final NamedParameterJdbcTemplate template;

    public JdbcScanDataRetentionRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public int deleteScannedProductsOlderThan(LocalDateTime cutoff) {
        return template.update(
                """
                delete from scanning_session_products
                where counted_date < :cutoff
                """,
                Map.of("cutoff", cutoff)
        );
    }

    @Override
    public int deleteEmptyScanningSessionsOlderThan(LocalDateTime cutoff) {
        return template.update(
                """
                delete s
                from scanning_sessions s
                left join scanning_session_products sp on sp.scanning_session_id = s.id
                where sp.scanning_session_id is null
                  and s.started_at < :cutoff
                  and s.closed_at is not null
                """,
                Map.of("cutoff", cutoff)
        );
    }
}
