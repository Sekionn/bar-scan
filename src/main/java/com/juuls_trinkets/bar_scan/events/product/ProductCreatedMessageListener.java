package com.juuls_trinkets.bar_scan.events.product;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.juuls_trinkets.bar_scan.dto.ProductCreatedEventDTO;
import com.juuls_trinkets.bar_scan.dto.ProductResponseDTO;
import com.juuls_trinkets.bar_scan.service.product.ProductApplicationService;
import com.juuls_trinkets.bar_scan.service.queuestatus.QueueStatusApplicationService;

import jakarta.validation.Valid;

@Component
public class ProductCreatedMessageListener {

    private final ProductApplicationService productService;
    private final QueueStatusApplicationService queueStatusService;

    public ProductCreatedMessageListener(
            ProductApplicationService productService,
            QueueStatusApplicationService queueStatusService
    ) {
        this.productService = productService;
        this.queueStatusService = queueStatusService;
    }

    @RabbitListener(queues = "${app.messaging.product-created-queue}")
    public void handle(@Valid ProductCreatedEventDTO event) {
        if (queueStatusService.isProcessed(event.messageId())) {
            return;
        }

        ProductResponseDTO product = productService.createProduct(event);
        queueStatusService.markProcessed(event.messageId(), event.syncBatchId(), product.companyId(), product.shopId());
    }
}
