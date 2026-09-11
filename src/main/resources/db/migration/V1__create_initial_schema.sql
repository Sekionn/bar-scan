CREATE TABLE companies
(
    id CHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    allowed_user_count INT NOT NULL,
    location_name VARCHAR(150) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_companies_allowed_user_count CHECK (allowed_user_count >= 0)
);

CREATE TABLE users
(
    id CHAR(36) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    company_id CHAR(36) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    KEY idx_users_company_id (company_id),
    CONSTRAINT fk_users_company
        FOREIGN KEY (company_id) REFERENCES companies(id)
        ON DELETE RESTRICT
);

CREATE TABLE products
(
    company_id CHAR(36) NOT NULL,
    product_id CHAR(36) NOT NULL,
    barcode VARCHAR(100) NOT NULL,
    shelf_of_origin INT NOT NULL,
    amount_counted INT NOT NULL,
    counted_date TIMESTAMP NOT NULL,
    PRIMARY KEY (company_id, product_id),
    KEY idx_products_barcode (barcode),
    CONSTRAINT fk_products_company
        FOREIGN KEY (company_id) REFERENCES companies(id)
        ON DELETE RESTRICT
);
