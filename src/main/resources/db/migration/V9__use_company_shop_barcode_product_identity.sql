ALTER TABLE products
    DROP INDEX uk_products_shop_barcode,
    ADD UNIQUE KEY uk_products_company_shop_barcode (company_id, shop_id, barcode);
