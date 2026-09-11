package com.juuls_trinkets.bar_scan.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.model.Product;

public interface ProductRepository {

    List<Product> findAllByCompanyId(UUID companyId);

    Optional<Product> findByCompanyIdAndBarcode(UUID companyId, String barcode);

    int create(Product product);

    Product addToCountByCompanyAndBarcode(Product product);

    int update(Product product);

    void delete(Product product);
}
