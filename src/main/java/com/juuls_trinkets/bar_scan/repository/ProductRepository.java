package com.juuls_trinkets.bar_scan.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.model.Product;

public interface ProductRepository {

    List<Product> findAllByCompanyId(UUID companyId);

    List<Product> findAllByCompanyIdAndQuarter(UUID companyId, int year, int quarter);

    boolean existsByShopIdAndBarcode(UUID shopId, String barcode);

    Optional<Product> findByCompanyIdAndBarcode(UUID companyId, String barcode);

    Optional<Product> findByShopIdAndBarcode(UUID shopId, String barcode);

    int create(Product product);

    Product addToCountByCompanyAndBarcode(Product product);

    Product addToCountByShopAndBarcode(Product product);

    int update(Product product);

    void delete(Product product);
}
