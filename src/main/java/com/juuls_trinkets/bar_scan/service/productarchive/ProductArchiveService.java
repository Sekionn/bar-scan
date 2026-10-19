package com.juuls_trinkets.bar_scan.service.productarchive;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.juuls_trinkets.bar_scan.dto.ProductArchiveDateResponseDTO;
import com.juuls_trinkets.bar_scan.dto.ProductArchiveItemResponseDTO;
import com.juuls_trinkets.bar_scan.dto.ProductArchivePageResponseDTO;
import com.juuls_trinkets.bar_scan.dto.ProductArchiveQuarterResponseDTO;
import com.juuls_trinkets.bar_scan.dto.ProductArchiveResponseDTO;
import com.juuls_trinkets.bar_scan.dto.ProductArchiveShopResponseDTO;
import com.juuls_trinkets.bar_scan.dto.ProductArchiveYearResponseDTO;
import com.juuls_trinkets.bar_scan.exception.BadRequestException;
import com.juuls_trinkets.bar_scan.exception.ResourceNotFoundException;
import com.juuls_trinkets.bar_scan.model.ProductArchiveSummaryRow;
import com.juuls_trinkets.bar_scan.model.Shop;
import com.juuls_trinkets.bar_scan.repository.ProductArchiveRepository;
import com.juuls_trinkets.bar_scan.repository.ShopRepository;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;

@Service
public class ProductArchiveService implements ProductArchiveApplicationService {

    private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yy");

    private final ProductArchiveRepository productArchiveRepository;
    private final ShopRepository shopRepository;

    public ProductArchiveService(ProductArchiveRepository productArchiveRepository, ShopRepository shopRepository) {
        this.productArchiveRepository = productArchiveRepository;
        this.shopRepository = shopRepository;
    }

    @Override
    public ProductArchiveResponseDTO getArchive(CustomUserDetails user) {
        List<Shop> visibleShops = visibleShops(user);
        UUID shopId = canSeeCompanyData(user) ? null : requireShop(user.getShopId());
        return buildArchive(visibleShops, productArchiveRepository.findSummary(user.getCompanyId(), shopId));
    }

    @Override
    public ProductArchivePageResponseDTO getProducts(
            CustomUserDetails user,
            UUID shopId,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    ) {
        UUID resolvedShopId = resolveReadableShopId(user, shopId);
        validateDateRange(startDate, endDate);
        int normalizedPage = Math.max(1, page);
        int normalizedSize = Math.min(200, Math.max(1, size));
        int offset = (normalizedPage - 1) * normalizedSize;

        long totalElements = productArchiveRepository.countProducts(user.getCompanyId(), resolvedShopId, startDate, endDate);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalElements / normalizedSize));

        return new ProductArchivePageResponseDTO(
                productArchiveRepository.findProducts(
                        user.getCompanyId(),
                        resolvedShopId,
                        startDate,
                        endDate,
                        normalizedSize,
                        offset
                ),
                normalizedPage,
                normalizedSize,
                totalElements,
                totalPages
        );
    }

    @Override
    public String exportProducts(
            CustomUserDetails user,
            UUID shopId,
            LocalDate startDate,
            LocalDate endDate,
            String format
    ) {
        UUID resolvedShopId = resolveReadableShopId(user, shopId);
        validateDateRange(startDate, endDate);
        List<ProductArchiveItemResponseDTO> products = productArchiveRepository.findProductsForExport(
                user.getCompanyId(),
                resolvedShopId,
                startDate,
                endDate
        );
        String normalizedFormat = format == null ? "csv" : format.toLowerCase();
        return switch (normalizedFormat) {
            case "txt" -> toTxt(products);
            case "csv" -> toCsv(products);
            default -> throw new BadRequestException("Unsupported export format");
        };
    }

    private ProductArchiveResponseDTO buildArchive(List<Shop> visibleShops, List<ProductArchiveSummaryRow> rows) {
        Map<UUID, ShopBucket> shops = new LinkedHashMap<>();

        for (Shop shop : visibleShops) {
            shops.put(shop.getId(), new ShopBucket(shop.getId(), shop.getName()));
        }

        for (ProductArchiveSummaryRow row : rows) {
            ShopBucket shop = shops.computeIfAbsent(row.shopId(), ignored -> new ShopBucket(row.shopId(), row.shopName()));
            YearBucket year = shop.years.computeIfAbsent(row.year(), YearBucket::new);
            QuarterBucket quarter = year.quarters.computeIfAbsent(row.quarter(), QuarterBucket::new);

            shop.productCount += row.productCount();
            year.productCount += row.productCount();
            quarter.productCount += row.productCount();
            quarter.dates.add(new ProductArchiveDateResponseDTO(row.date(), row.productCount()));
        }

        return new ProductArchiveResponseDTO(shops.values().stream()
                .map(ShopBucket::toResponse)
                .toList());
    }

    private List<Shop> visibleShops(CustomUserDetails user) {
        if (canSeeCompanyData(user)) {
            return shopRepository.findAllByCompanyId(user.getCompanyId());
        }
        UUID shopId = requireShop(user.getShopId());
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        if (!user.getCompanyId().equals(shop.getCompanyId())) {
            throw new ResourceNotFoundException("Shop not found");
        }
        return List.of(shop);
    }

    private UUID resolveReadableShopId(CustomUserDetails user, UUID requestedShopId) {
        if (canSeeCompanyData(user)) {
            if (requestedShopId == null) {
                throw new BadRequestException("shopId is required");
            }
            Shop shop = shopRepository.findById(requestedShopId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
            if (!user.getCompanyId().equals(shop.getCompanyId())) {
                throw new ResourceNotFoundException("Shop not found");
            }
            return requestedShopId;
        }

        UUID ownShopId = requireShop(user.getShopId());
        if (requestedShopId != null && !ownShopId.equals(requestedShopId)) {
            throw new ResourceNotFoundException("Shop not found");
        }
        return ownShopId;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date must be on or after start date");
        }
    }

    private UUID requireShop(UUID shopId) {
        if (shopId == null) {
            throw new ResourceNotFoundException("User is not assigned to a shop");
        }
        return shopId;
    }

    private boolean canSeeCompanyData(CustomUserDetails user) {
        String role = user.getAuthorities().iterator().next().getAuthority();
        return "ROLE_DATA_ANALYST".equals(role)
                || "ROLE_ADMIN".equals(role)
                || "ROLE_MANAGER".equals(role)
                || "ROLE_SUPER_ADMIN".equals(role);
    }

    private String toCsv(List<ProductArchiveItemResponseDTO> products) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < products.size(); index++) {
            ProductArchiveItemResponseDTO product = products.get(index);
            builder.append(csv(product.storeSegment())).append(';')
                    .append(csvText(product.barcode())).append(';')
                    .append(product.amountCounted()).append(';')
                    .append(csv(formatExportDate(product.date()))).append(';')
                    .append(index + 1).append('\n');
        }
        return builder.toString();
    }

    private String toTxt(List<ProductArchiveItemResponseDTO> products) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < products.size(); index++) {
            ProductArchiveItemResponseDTO product = products.get(index);
            builder.append(product.storeSegment()).append('|')
                    .append(product.barcode()).append('|')
                    .append(product.amountCounted()).append('|')
                    .append(formatExportDate(product.date())).append('|')
                    .append(index + 1).append('\n');
        }
        return builder.toString();
    }

    private String formatExportDate(LocalDate date) {
        return date.format(EXPORT_DATE_FORMAT);
    }

    private String csv(String value) {
        if (!value.contains(";") && !value.contains("\"") && !value.contains("\n") && !value.contains("\r")) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String csvText(String value) {
        return "=\"" + value.replace("\"", "\"\"") + "\"";
    }

}
