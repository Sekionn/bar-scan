package com.juuls_trinkets.bar_scan.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.juuls_trinkets.bar_scan.dto.ProductArchivePageResponseDTO;
import com.juuls_trinkets.bar_scan.dto.ProductArchiveResponseDTO;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;
import com.juuls_trinkets.bar_scan.service.productarchive.ProductArchiveApplicationService;

@RestController
@RequestMapping("/products/archive")
public class ProductArchiveController {

    private final ProductArchiveApplicationService productArchiveService;

    public ProductArchiveController(ProductArchiveApplicationService productArchiveService) {
        this.productArchiveService = productArchiveService;
    }

    @GetMapping
    public ProductArchiveResponseDTO getArchive(@AuthenticationPrincipal CustomUserDetails user) {
        return productArchiveService.getArchive(user);
    }

    @GetMapping("/products")
    public ProductArchivePageResponseDTO getProducts(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam UUID shopId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String quarter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return productArchiveService.getProducts(user, shopId, year, quarter, date, page, size);
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportProducts(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam UUID shopId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String quarter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept
    ) {
        String format = exportFormat(accept);
        MediaType contentType = "txt".equals(format)
                ? MediaType.TEXT_PLAIN
                : MediaType.parseMediaType("text/csv");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bar-scan-products." + format + "\"")
                .contentType(contentType)
                .body(productArchiveService.exportProducts(user, shopId, year, quarter, date, format));
    }

    private String exportFormat(String accept) {
        if (accept != null && accept.toLowerCase().contains(MediaType.TEXT_PLAIN_VALUE)) {
            return "txt";
        }
        return "csv";
    }
}
