package com.juuls_trinkets.bar_scan.service.productarchive;

import java.time.LocalDate;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.dto.ProductArchivePageResponseDTO;
import com.juuls_trinkets.bar_scan.dto.ProductArchiveResponseDTO;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;

public interface ProductArchiveApplicationService {

    ProductArchiveResponseDTO getArchive(CustomUserDetails user);

    ProductArchivePageResponseDTO getProducts(
            CustomUserDetails user,
            UUID shopId,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    );

    String exportProducts(
            CustomUserDetails user,
            UUID shopId,
            LocalDate startDate,
            LocalDate endDate,
            String format
    );
}
