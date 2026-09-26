CREATE TABLE shops
(
    id CHAR(36) NOT NULL,
    company_id CHAR(36) NOT NULL,
    name VARCHAR(150) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_shops_company_name (company_id, name),
    KEY idx_shops_company_id (company_id),
    CONSTRAINT fk_shops_company
        FOREIGN KEY (company_id) REFERENCES companies(id)
        ON DELETE RESTRICT
);

INSERT INTO shops(id, company_id, name)
SELECT UUID(), c.id, c.location_name
FROM companies c
WHERE NOT EXISTS (
    SELECT 1
    FROM shops s
    WHERE s.company_id = c.id
);

ALTER TABLE users
    ADD COLUMN shop_id CHAR(36) NULL,
    ADD KEY idx_users_shop_id (shop_id),
    ADD CONSTRAINT fk_users_shop
        FOREIGN KEY (shop_id) REFERENCES shops(id)
        ON DELETE RESTRICT;

UPDATE users u
JOIN shops s ON s.company_id = u.company_id
SET u.shop_id = s.id
WHERE u.shop_id IS NULL
  AND u.role IN ('ROLE_USER', 'ROLE_ADMIN');

ALTER TABLE products
    DROP INDEX uk_products_company_barcode,
    ADD COLUMN shop_id CHAR(36) NULL,
    ADD COLUMN store_segment VARCHAR(100) NULL,
    ADD KEY idx_products_shop_id (shop_id),
    ADD CONSTRAINT fk_products_shop
        FOREIGN KEY (shop_id) REFERENCES shops(id)
        ON DELETE RESTRICT;

UPDATE products p
JOIN shops s ON s.company_id = p.company_id
SET p.shop_id = s.id,
    p.store_segment = CAST(p.shelf_of_origin AS CHAR)
WHERE p.shop_id IS NULL;

ALTER TABLE products
    MODIFY shop_id CHAR(36) NOT NULL,
    MODIFY store_segment VARCHAR(100) NOT NULL,
    ADD UNIQUE KEY uk_products_shop_barcode (shop_id, barcode);
