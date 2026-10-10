ALTER TABLE shops
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN active_name VARCHAR(150) GENERATED ALWAYS AS (
        CASE
            WHEN active THEN name
            ELSE NULL
        END
    ) STORED;

ALTER TABLE shops
    DROP INDEX uk_shops_company_name;

ALTER TABLE shops
    ADD UNIQUE KEY uk_shops_company_active_name (company_id, active_name);

CREATE INDEX idx_shops_company_active ON shops(company_id, active);
