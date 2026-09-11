package com.juuls_trinkets.bar_scan.repository;

import java.util.List;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.model.Product;

public interface ProductRepository {

    List<Product> findAllByCompanyId(UUID companyId);

    int create(Product product);

    int update(Product product);

    void delete(Product product);
}
