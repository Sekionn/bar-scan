CREATE TABLE scanning_sessions
(
    id CHAR(36) NOT NULL,
    company_id CHAR(36) NOT NULL,
    shop_id CHAR(36) NOT NULL,
    started_by_user_id CHAR(36) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP NULL,
    open_shop_id CHAR(36) GENERATED ALWAYS AS (
        CASE
            WHEN closed_at IS NULL THEN shop_id
            ELSE NULL
        END
    ) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uk_scanning_sessions_open_shop (open_shop_id),
    KEY idx_scanning_sessions_company_shop_started (company_id, shop_id, started_at),
    KEY idx_scanning_sessions_shop_closed (shop_id, closed_at),
    CONSTRAINT fk_scanning_sessions_company
        FOREIGN KEY (company_id) REFERENCES companies(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_scanning_sessions_shop
        FOREIGN KEY (shop_id) REFERENCES shops(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_scanning_sessions_started_by_user
        FOREIGN KEY (started_by_user_id) REFERENCES users(id)
        ON DELETE RESTRICT
);

CREATE TABLE scanning_session_products
(
    scanning_session_id CHAR(36) NOT NULL,
    product_id CHAR(36) NOT NULL,
    barcode VARCHAR(100) NOT NULL,
    shelf_of_origin INT NOT NULL,
    store_segment VARCHAR(100) NOT NULL,
    amount_counted INT NOT NULL,
    counted_date TIMESTAMP NOT NULL,
    PRIMARY KEY (scanning_session_id, product_id),
    UNIQUE KEY uk_scanning_session_products_session_barcode (scanning_session_id, barcode),
    KEY idx_scanning_session_products_barcode (barcode),
    CONSTRAINT fk_scanning_session_products_session
        FOREIGN KEY (scanning_session_id) REFERENCES scanning_sessions(id)
        ON DELETE CASCADE
);

ALTER TABLE products
    ADD COLUMN scanning_session_id CHAR(36) NULL,
    ADD KEY idx_products_scanning_session_id (scanning_session_id),
    ADD CONSTRAINT fk_products_scanning_session
        FOREIGN KEY (scanning_session_id) REFERENCES scanning_sessions(id)
        ON DELETE SET NULL;
