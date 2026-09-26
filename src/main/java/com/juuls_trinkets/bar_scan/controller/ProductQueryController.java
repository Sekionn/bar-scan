package com.juuls_trinkets.bar_scan.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.juuls_trinkets.bar_scan.dto.ProductAvailabilityResponseDTO;
import com.juuls_trinkets.bar_scan.dto.ProductResponseDTO;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;
import com.juuls_trinkets.bar_scan.service.product.ProductApplicationService;

@RestController
@RequestMapping("/products")
public class ProductQueryController {

    private final ProductApplicationService productService;

    public ProductQueryController(ProductApplicationService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponseDTO> getProducts(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) String quarter
    ) {
        return productService.findProducts(user, quarter);
    }

    @GetMapping("/availability")
    public ProductAvailabilityResponseDTO getAvailability(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam String barcode
    ) {
        return new ProductAvailabilityResponseDTO(
                barcode,
                productService.isStoredForUserShop(user, barcode)
        );
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportProducts(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) String quarter,
            @RequestParam(defaultValue = "csv") String format
    ) {
        MediaType mediaType = "txt".equalsIgnoreCase(format)
                ? MediaType.TEXT_PLAIN
                : MediaType.parseMediaType("text/csv");
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(productService.exportProducts(user, quarter, format));
    }
}
