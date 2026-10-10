package com.juuls_trinkets.bar_scan.service.product;

import com.juuls_trinkets.bar_scan.dto.ProductCreatedEventDTO;
import com.juuls_trinkets.bar_scan.dto.ProductResponseDTO;

public interface ProductApplicationService {

    ProductResponseDTO createProduct(ProductCreatedEventDTO event);
}
