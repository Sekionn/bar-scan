package com.juuls_trinkets.bar_scan.service.product;

import java.util.List;

import com.juuls_trinkets.bar_scan.dto.ProductCreatedEventDTO;
import com.juuls_trinkets.bar_scan.dto.ProductResponseDTO;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;

public interface ProductApplicationService {

    List<ProductResponseDTO> findProducts(CustomUserDetails user, String quarter);

    ProductResponseDTO createProduct(ProductCreatedEventDTO event);

    boolean isStoredForUserShop(CustomUserDetails user, String barcode);

    String exportProducts(CustomUserDetails user, String quarter, String format);
}
