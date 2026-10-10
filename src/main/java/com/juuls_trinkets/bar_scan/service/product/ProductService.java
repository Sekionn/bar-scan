package com.juuls_trinkets.bar_scan.service.product;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juuls_trinkets.bar_scan.dto.ProductCreatedEventDTO;
import com.juuls_trinkets.bar_scan.dto.ProductResponseDTO;
import com.juuls_trinkets.bar_scan.exception.DuplicateResourceException;
import com.juuls_trinkets.bar_scan.exception.ResourceNotFoundException;
import com.juuls_trinkets.bar_scan.model.AppUser;
import com.juuls_trinkets.bar_scan.model.ScanningSession;
import com.juuls_trinkets.bar_scan.model.ScanningSessionProduct;
import com.juuls_trinkets.bar_scan.repository.ScanningSessionRepository;
import com.juuls_trinkets.bar_scan.repository.UserRepository;

@Service
public class ProductService implements ProductApplicationService {

    private final UserRepository userRepository;
    private final ScanningSessionRepository scanningSessionRepository;

    public ProductService(
            UserRepository userRepository,
            ScanningSessionRepository scanningSessionRepository
    ) {
        this.userRepository = userRepository;
        this.scanningSessionRepository = scanningSessionRepository;
    }

    @Transactional
    @Override
    public ProductResponseDTO createProduct(ProductCreatedEventDTO event) {
        AppUser user = userRepository.findById(event.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for product created event"));

        UUID shopId = requireShop(user.getShopId());
        ScanningSession scanningSession = findOrCreateOpenSession(user, shopId);
        String storeSegment = event.storeSegment() == null || event.storeSegment().isBlank()
                ? String.valueOf(event.shelfOfOrigin())
                : event.storeSegment().trim();

        ScanningSessionProduct product = new ScanningSessionProduct(
                scanningSession.getId(),
                event.messageId(),
                event.barcode(),
                event.shelfOfOrigin(),
                storeSegment,
                event.amountCounted(),
                LocalDateTime.now(ZoneOffset.UTC)
        );
        ScanningSessionProduct savedProduct = scanningSessionRepository.addProductScan(product);
        return new ProductResponseDTO(user.getCompanyId(), shopId, savedProduct);
    }

    private ScanningSession findOrCreateOpenSession(AppUser user, UUID shopId) {
        return scanningSessionRepository.findOpenByShopId(shopId)
                .orElseGet(() -> createOpenSession(user, shopId));
    }

    private ScanningSession createOpenSession(AppUser user, UUID shopId) {
        try {
            return scanningSessionRepository.create(new ScanningSession(
                    UUID.randomUUID(),
                    user.getCompanyId(),
                    shopId,
                    user.getId(),
                    LocalDateTime.now(ZoneOffset.UTC),
                    null
            ));
        } catch (DuplicateResourceException ex) {
            return scanningSessionRepository.findOpenByShopId(shopId)
                    .orElseThrow(() -> ex);
        }
    }

    private UUID requireShop(UUID shopId) {
        if (shopId == null) {
            throw new ResourceNotFoundException("User is not assigned to a shop");
        }
        return shopId;
    }

}
