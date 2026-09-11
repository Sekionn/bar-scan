CREATE TEMPORARY TABLE product_barcode_rollups AS
SELECT
    company_id,
    barcode,
    SUBSTRING_INDEX(GROUP_CONCAT(product_id ORDER BY counted_date DESC, product_id DESC), ',', 1) AS product_id,
    SUBSTRING_INDEX(GROUP_CONCAT(shelf_of_origin ORDER BY counted_date DESC, product_id DESC), ',', 1) AS shelf_of_origin,
    SUM(amount_counted) AS amount_counted,
    MAX(counted_date) AS counted_date
FROM products
GROUP BY company_id, barcode;

DELETE p
FROM products p
JOIN product_barcode_rollups r
    ON r.company_id = p.company_id
   AND r.barcode = p.barcode
WHERE p.product_id <> r.product_id;

UPDATE products p
JOIN product_barcode_rollups r
    ON r.company_id = p.company_id
   AND r.product_id = p.product_id
SET p.shelf_of_origin = r.shelf_of_origin,
    p.amount_counted = r.amount_counted,
    p.counted_date = r.counted_date;

ALTER TABLE products
    ADD UNIQUE KEY uk_products_company_barcode (company_id, barcode);
