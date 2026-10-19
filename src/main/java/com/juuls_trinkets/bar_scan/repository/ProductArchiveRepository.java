package com.juuls_trinkets.bar_scan.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.dto.ProductArchiveItemResponseDTO;
import com.juuls_trinkets.bar_scan.model.ProductArchiveSummaryRow;

public interface ProductArchiveRepository {

    List<ProductArchiveSummaryRow> findSummary(UUID companyId, UUID shopId);

    long countProducts(UUID companyId, UUID shopId, LocalDate startDate, LocalDate endDate);

    List<ProductArchiveItemResponseDTO> findProducts(
            UUID companyId,
            UUID shopId,
            LocalDate startDate,
            LocalDate endDate,
            int limit,
            int offset
    );

    List<ProductArchiveItemResponseDTO> findProductsForExport(
            UUID companyId,
            UUID shopId,
            LocalDate startDate,
            LocalDate endDate
    );
}
