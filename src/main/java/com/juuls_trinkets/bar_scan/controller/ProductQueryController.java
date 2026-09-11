package com.juuls_trinkets.bar_scan.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juuls_trinkets.bar_scan.dto.ProductResponseDTO;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;
import com.juuls_trinkets.bar_scan.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductQueryController {

    private final ProductService productService;

    public ProductQueryController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponseDTO> getProducts(@AuthenticationPrincipal CustomUserDetails user) {
        return productService.findAllByCompanyId(user.getCompanyId());
    }
}
