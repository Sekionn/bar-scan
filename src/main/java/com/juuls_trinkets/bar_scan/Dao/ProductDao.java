package com.juuls_trinkets.bar_scan.Dao;

import java.util.List;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.models.Product;

public interface ProductDao {

List<Product> findAll();

int insertProduct(Product product);

int updateProduct(Product product);

int executeUpdateProduct(Product product);

public void deleteProduct(Product product);
}
