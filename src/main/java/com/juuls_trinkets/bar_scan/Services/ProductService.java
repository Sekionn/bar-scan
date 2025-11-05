package com.juuls_trinkets.bar_scan.Services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.juuls_trinkets.bar_scan.Dao.ProductDaoImpl;
import com.juuls_trinkets.bar_scan.models.Product;

@Service
public class ProductService {

    @Autowired
    ProductDaoImpl productRepository;


	public List<Product> findAll() {
		return productRepository.findAll();
	}

	public int createProduct(Product product) {
        return productRepository.insertProduct(product);
	}
}