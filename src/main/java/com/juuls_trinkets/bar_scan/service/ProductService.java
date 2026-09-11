package com.juuls_trinkets.bar_scan.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juuls_trinkets.bar_scan.dto.ProductCreatedEventDTO;
import com.juuls_trinkets.bar_scan.dto.ProductResponseDTO;
import com.juuls_trinkets.bar_scan.exception.ResourceNotFoundException;
import com.juuls_trinkets.bar_scan.model.AppUser;
import com.juuls_trinkets.bar_scan.model.Product;
import com.juuls_trinkets.bar_scan.repository.ProductRepository;
import com.juuls_trinkets.bar_scan.repository.UserRepository;

@Service
public class ProductService {

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

    @Transactional
    public ProductResponseDTO createProduct(ProductCreatedEventDTO event) {
        AppUser user = userRepository.findById(event.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for product created event"));

        Product product = new Product(
                user.getCompanyId(),
                event.productId(),
                event.barcode(),
                event.shelfOfOrigin(),
                event.amountCounted()
        );
        productRepository.create(product);
        return new ProductResponseDTO(product);
    }
}
