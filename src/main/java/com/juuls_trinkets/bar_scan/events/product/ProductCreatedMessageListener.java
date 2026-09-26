package com.juuls_trinkets.bar_scan.events.product;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.juuls_trinkets.bar_scan.dto.ProductCreatedEventDTO;
import com.juuls_trinkets.bar_scan.service.product.ProductApplicationService;

import jakarta.validation.Valid;

@Component
public class ProductCreatedMessageListener {

    private final ProductApplicationService productService;

    public ProductCreatedMessageListener(ProductApplicationService productService) {
        this.productService = productService;
    }

    @RabbitListener(queues = "${app.messaging.product-created-queue}")
    public void handle(@Valid ProductCreatedEventDTO event) {
        productService.createProduct(event);
    }
}
