ALTER TABLE products
    ADD COLUMN message_id CHAR(36) NULL;

UPDATE products
SET message_id = UUID()
WHERE message_id IS NULL;

ALTER TABLE products
    MODIFY message_id CHAR(36) NOT NULL,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (message_id),
    ADD KEY idx_products_company_shop_barcode (company_id, shop_id, barcode);

ALTER TABLE scanning_session_products
    ADD COLUMN message_id CHAR(36) NULL;

UPDATE scanning_session_products
SET message_id = UUID()
WHERE message_id IS NULL;

ALTER TABLE scanning_session_products
    MODIFY message_id CHAR(36) NOT NULL,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (message_id),
    ADD KEY idx_scanning_session_products_session_counted (scanning_session_id, counted_date),
    ADD KEY idx_scanning_session_products_session_barcode (scanning_session_id, barcode);
