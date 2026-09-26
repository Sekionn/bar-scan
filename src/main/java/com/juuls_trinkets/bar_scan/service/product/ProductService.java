package com.juuls_trinkets.bar_scan.service.product;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juuls_trinkets.bar_scan.dto.ProductCreatedEventDTO;
import com.juuls_trinkets.bar_scan.dto.ProductResponseDTO;
import com.juuls_trinkets.bar_scan.exception.BadRequestException;
import com.juuls_trinkets.bar_scan.exception.ResourceNotFoundException;
import com.juuls_trinkets.bar_scan.model.AppUser;
import com.juuls_trinkets.bar_scan.model.Product;
import com.juuls_trinkets.bar_scan.repository.ProductRepository;
import com.juuls_trinkets.bar_scan.repository.UserRepository;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;

@Service
public class ProductService implements ProductApplicationService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<ProductResponseDTO> findAllByCompanyId(UUID companyId) {
        return productRepository.findAllByCompanyId(companyId).stream()
                .map(ProductResponseDTO::new)
                .toList();
    }

    @Override
    public List<ProductResponseDTO> findProducts(CustomUserDetails user, String quarter) {
        List<Product> products = quarter == null || quarter.isBlank()
                ? productRepository.findAllByCompanyId(user.getCompanyId())
                : findByQuarter(user.getCompanyId(), quarter);

        if (!canSeeCompanyData(user)) {
            UUID shopId = requireShop(user.getShopId());
            products = products.stream()
                    .filter(product -> shopId.equals(product.getShopId()))
                    .toList();
        }

        return products.stream()
                .map(ProductResponseDTO::new)
                .toList();
    }

    @Transactional
    @Override
    public ProductResponseDTO createProduct(ProductCreatedEventDTO event) {
        AppUser user = userRepository.findById(event.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for product created event"));

        UUID shopId = requireShop(user.getShopId());
        String storeSegment = event.storeSegment() == null || event.storeSegment().isBlank()
                ? String.valueOf(event.shelfOfOrigin())
                : event.storeSegment().trim();

        Product product = new Product(
                user.getCompanyId(),
                shopId,
                event.productId(),
                event.barcode(),
                event.shelfOfOrigin(),
                storeSegment,
                event.amountCounted()
        );
        return new ProductResponseDTO(productRepository.addToCountByShopAndBarcode(product));
    }

    @Override
    public boolean isStoredForUserShop(CustomUserDetails user, String barcode) {
        return productRepository.existsByShopIdAndBarcode(requireShop(user.getShopId()), barcode);
    }

    @Override
    public String exportProducts(CustomUserDetails user, String quarter, String format) {
        List<ProductResponseDTO> products = findProducts(user, quarter);
        String normalizedFormat = format == null ? "csv" : format.toLowerCase();
        return switch (normalizedFormat) {
            case "txt" -> toTxt(products);
            case "csv", "excel" -> toCsv(products);
            default -> throw new BadRequestException("Unsupported export format");
        };
    }

    private List<Product> findByQuarter(UUID companyId, String quarter) {
        String[] parts = quarter.split("-Q");
        if (parts.length != 2) {
            throw new BadRequestException("Quarter must use format YYYY-QN");
        }
        int year = Integer.parseInt(parts[0]);
        int quarterNumber = Integer.parseInt(parts[1]);
        if (quarterNumber < 1 || quarterNumber > 4) {
            throw new BadRequestException("Quarter must be between Q1 and Q4");
        }
        return productRepository.findAllByCompanyIdAndQuarter(companyId, year, quarterNumber);
    }

    private UUID requireShop(UUID shopId) {
        if (shopId == null) {
            throw new ResourceNotFoundException("User is not assigned to a shop");
        }
        return shopId;
    }

    private boolean canSeeCompanyData(CustomUserDetails user) {
        String role = user.getAuthorities().iterator().next().getAuthority();
        return "ROLE_MANAGER".equals(role) || "ROLE_SUPER_ADMIN".equals(role);
    }

    private String toCsv(List<ProductResponseDTO> products) {
        StringBuilder builder = new StringBuilder("companyId,shopId,productId,barcode,storeSegment,amountCounted,countedDate,countedQuarter\n");
        for (ProductResponseDTO product : products) {
            builder.append(product.companyId()).append(',')
                    .append(product.shopId()).append(',')
                    .append(product.productId()).append(',')
                    .append(product.barcode()).append(',')
                    .append(product.storeSegment()).append(',')
                    .append(product.amountCounted()).append(',')
                    .append(product.countedDate()).append(',')
                    .append(product.countedQuarter()).append('\n');
        }
        return builder.toString();
    }

    private String toTxt(List<ProductResponseDTO> products) {
        StringBuilder builder = new StringBuilder();
        for (ProductResponseDTO product : products) {
            builder.append(product.barcode())
                    .append('\t')
                    .append(product.storeSegment())
                    .append('\t')
                    .append(product.amountCounted())
                    .append('\t')
                    .append(product.countedDate())
                    .append('\n');
        }
        return builder.toString();
    }
}
