CREATE TABLE queue_message_statuses (
    message_id CHAR(36) PRIMARY KEY,
    company_id CHAR(36) NOT NULL,
    shop_id CHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    published_at TIMESTAMP NULL,
    processed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_queue_message_statuses_company_shop_status (company_id, shop_id, status),
    KEY idx_queue_message_statuses_updated_at (updated_at),
    CONSTRAINT fk_queue_message_statuses_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_queue_message_statuses_shop FOREIGN KEY (shop_id) REFERENCES shops(id)
);
