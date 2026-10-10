ALTER TABLE queue_message_statuses
    ADD COLUMN sync_batch_id CHAR(36) NULL AFTER message_id,
    ADD KEY idx_queue_message_statuses_company_shop_batch_status (company_id, shop_id, sync_batch_id, status);
