package com.juuls_trinkets.bar_scan.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.juuls_trinkets.bar_scan.dto.QueueStatusResponseDTO;

@Repository
public class JdbcQueueStatusRepository implements QueueStatusRepository {

    private final NamedParameterJdbcTemplate template;

    public JdbcQueueStatusRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public int registerExpected(UUID messageId, UUID syncBatchId, UUID companyId, UUID shopId) {
        return template.update(
                """
                insert into queue_message_statuses(message_id, sync_batch_id, company_id, shop_id, status, published_at)
                values(:messageId, :syncBatchId, :companyId, :shopId, 'PENDING', current_timestamp)
                on duplicate key update
                    sync_batch_id = coalesce(sync_batch_id, values(sync_batch_id)),
                    published_at = coalesce(published_at, values(published_at)),
                    status = case
                        when status = 'PROCESSED' then status
                        else 'PENDING'
                    end
                """,
                parameters(messageId, syncBatchId, companyId, shopId)
        );
    }

    @Override
    public void cancelUnpublished(UUID messageId, UUID syncBatchId, UUID companyId, UUID shopId) {
        template.update(
                """
                update queue_message_statuses
                set status = 'CANCELED'
                where message_id = :messageId
                  and sync_batch_id = :syncBatchId
                  and company_id = :companyId
                  and shop_id = :shopId
                  and status = 'PENDING'
                """,
                parameters(messageId, syncBatchId, companyId, shopId)
        );
    }

    @Override
    public void markProcessed(UUID messageId, UUID syncBatchId, UUID companyId, UUID shopId) {
        template.update(
                """
                insert into queue_message_statuses(message_id, sync_batch_id, company_id, shop_id, status, processed_at)
                values(:messageId, :syncBatchId, :companyId, :shopId, 'PROCESSED', current_timestamp)
                on duplicate key update
                    sync_batch_id = coalesce(sync_batch_id, values(sync_batch_id)),
                    company_id = values(company_id),
                    shop_id = values(shop_id),
                    status = 'PROCESSED',
                    processed_at = current_timestamp
                """,
                parameters(messageId, syncBatchId, companyId, shopId)
        );
    }

    @Override
    public boolean isProcessed(UUID messageId) {
        Boolean processed = template.queryForObject(
                """
                select exists(
                    select 1
                    from queue_message_statuses
                    where message_id = :messageId
                      and status = 'PROCESSED'
                )
                """,
                Map.of("messageId", messageId.toString()),
                Boolean.class
        );
        return Boolean.TRUE.equals(processed);
    }

    @Override
    public QueueStatusResponseDTO findStatus(UUID companyId, UUID shopId) {
        List<QueueStatusResponseDTO> statuses = template.query(
                """
                select :companyId as company_id,
                       :shopId as shop_id,
                       coalesce(sum(case when status = 'PENDING' then 1 else 0 end), 0) as pending_messages,
                       coalesce(sum(case when status = 'PROCESSED' then 1 else 0 end), 0) as processed_messages,
                       coalesce(count(*), 0) as active_messages
                from queue_message_statuses
                where company_id = :companyId
                  and shop_id = :shopId
                  and sync_batch_id in (
                      select sync_batch_id
                      from queue_message_statuses
                      where company_id = :companyId
                        and shop_id = :shopId
                        and status = 'PENDING'
                        and sync_batch_id is not null
                  )
                  and status in ('PENDING', 'PROCESSED')
                """,
                Map.of(
                        "companyId", companyId.toString(),
                        "shopId", shopId.toString()
                ),
                (result, rowNumber) -> mapStatus(result.getString("company_id"), result.getString("shop_id"), result.getLong("pending_messages"), result.getLong("processed_messages"), result.getLong("active_messages"))
        );
        return statuses.get(0);
    }

    @Override
    public List<QueueStatusResponseDTO> findStatuses(UUID companyId) {
        return template.query(
                """
                select
                    company_id,
                    shop_id,
                    sum(case when status = 'PENDING' then 1 else 0 end) as pending_messages,
                    sum(case when status = 'PROCESSED' then 1 else 0 end) as processed_messages,
                    count(*) as active_messages
                from queue_message_statuses
                where company_id = :companyId
                  and sync_batch_id in (
                      select sync_batch_id
                      from queue_message_statuses
                      where company_id = :companyId
                        and status = 'PENDING'
                        and sync_batch_id is not null
                  )
                  and status in ('PENDING', 'PROCESSED')
                group by company_id, shop_id
                order by shop_id
                """,
                Map.of("companyId", companyId.toString()),
                (result, rowNumber) -> mapStatus(result.getString("company_id"), result.getString("shop_id"), result.getLong("pending_messages"), result.getLong("processed_messages"), result.getLong("active_messages"))
        );
    }

    private QueueStatusResponseDTO mapStatus(
            String companyId,
            String shopId,
            long pendingMessages,
            long processedMessages,
            long activeMessages
    ) {
        int pendingPercentage = activeMessages == 0
                ? 0
                : (int) Math.ceil((pendingMessages * 100.0) / activeMessages);
        return new QueueStatusResponseDTO(
                UUID.fromString(companyId),
                UUID.fromString(shopId),
                pendingMessages,
                processedMessages,
                pendingPercentage,
                pendingMessages == 0
        );
    }

    private MapSqlParameterSource parameters(UUID messageId, UUID syncBatchId, UUID companyId, UUID shopId) {
        return new MapSqlParameterSource()
                .addValue("messageId", messageId.toString())
                .addValue("syncBatchId", syncBatchId == null ? null : syncBatchId.toString())
                .addValue("companyId", companyId.toString())
                .addValue("shopId", shopId.toString());
    }
}
