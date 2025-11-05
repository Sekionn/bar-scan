package com.juuls_trinkets.bar_scan;

import java.util.LinkedList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.juuls_trinkets.bar_scan.Services.ProductService;
import com.juuls_trinkets.bar_scan.models.MultipleProductsDTO;
import com.juuls_trinkets.bar_scan.models.Product;
import com.juuls_trinkets.bar_scan.models.ProductCreationDTO;

import jakarta.annotation.Resource;

@RestController
public class ProductController {

    @Resource 
    ProductService productService;

    //Get all products
    @GetMapping("/products")
    List<Product> getProducts(){

        return productService.findAll();
    }

    // create single new product
    @PostMapping("/products")
    Product createProduct(@RequestBody ProductCreationDTO productDto){
        Product product = productDto.createProduct();
        productService.createProduct(product);
        return product;
    }

    // create multiple new product
    @PostMapping("/products/multiple")
    LinkedList<Product> createProducts(@RequestBody MultipleProductsDTO productsDto){
        LinkedList<Product> products = new LinkedList<Product>();

        for (ProductCreationDTO product : productsDto.products) {
            products.add(product.createProduct());
        }

        return products;
    }
}
