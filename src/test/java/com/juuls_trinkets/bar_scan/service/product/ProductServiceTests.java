package com.juuls_trinkets.bar_scan.service.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.juuls_trinkets.bar_scan.dto.ProductCreatedEventDTO;
import com.juuls_trinkets.bar_scan.model.AppUser;
import com.juuls_trinkets.bar_scan.model.ScanningSession;
import com.juuls_trinkets.bar_scan.model.ScanningSessionProduct;
import com.juuls_trinkets.bar_scan.repository.ScanningSessionRepository;
import com.juuls_trinkets.bar_scan.repository.UserRepository;

class ProductServiceTests {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final ScanningSessionRepository scanningSessionRepository = mock(ScanningSessionRepository.class);
    private final ProductService service = new ProductService(userRepository, scanningSessionRepository);

    @Test
    void createsSeparateScanRowsForRepeatedBarcodeMessages() {
        UUID companyId = UUID.randomUUID();
        UUID shopId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID syncBatchId = UUID.randomUUID();
        UUID firstMessageId = UUID.randomUUID();
        UUID secondMessageId = UUID.randomUUID();
        AppUser user = new AppUser(userId, "scanner", "hash", companyId, shopId, "ROLE_USER");
        ScanningSession session = new ScanningSession(sessionId, companyId, shopId, userId, LocalDateTime.now(), null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(scanningSessionRepository.findOpenByShopId(shopId)).thenReturn(Optional.of(session));
        when(scanningSessionRepository.addProductScan(any(ScanningSessionProduct.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createProduct(new ProductCreatedEventDTO(firstMessageId, syncBatchId, userId, "5713428006725", 1, "01", 7));
        service.createProduct(new ProductCreatedEventDTO(secondMessageId, syncBatchId, userId, "5713428006725", 1, "01", 4));

        ArgumentCaptor<ScanningSessionProduct> productCaptor = ArgumentCaptor.forClass(ScanningSessionProduct.class);
        verify(scanningSessionRepository, times(2)).addProductScan(productCaptor.capture());
        assertThat(productCaptor.getAllValues())
                .extracting(ScanningSessionProduct::getMessageId)
                .containsExactly(firstMessageId, secondMessageId);
        assertThat(productCaptor.getAllValues())
                .extracting(ScanningSessionProduct::getBarcode)
                .containsExactly("5713428006725", "5713428006725");
        assertThat(productCaptor.getAllValues())
                .extracting(ScanningSessionProduct::getAmountCounted)
                .containsExactly(7, 4);
    }
}
