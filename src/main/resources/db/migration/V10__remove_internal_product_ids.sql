ALTER TABLE scanning_session_products
    DROP PRIMARY KEY,
    DROP COLUMN product_id,
    ADD PRIMARY KEY (scanning_session_id, barcode);

ALTER TABLE scanning_session_products
    DROP INDEX uk_scanning_session_products_session_barcode;

ALTER TABLE products
    ADD KEY idx_products_company_id (company_id);

ALTER TABLE products
    DROP PRIMARY KEY,
    DROP INDEX uk_products_company_shop_barcode,
    DROP COLUMN product_id,
    ADD PRIMARY KEY (company_id, shop_id, barcode);
