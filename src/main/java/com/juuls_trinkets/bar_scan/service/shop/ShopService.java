package com.juuls_trinkets.bar_scan.service.shop;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juuls_trinkets.bar_scan.dto.CreateShopRequestDTO;
import com.juuls_trinkets.bar_scan.dto.ShopResponseDTO;
import com.juuls_trinkets.bar_scan.exception.DuplicateResourceException;
import com.juuls_trinkets.bar_scan.exception.ResourceNotFoundException;
import com.juuls_trinkets.bar_scan.model.Shop;
import com.juuls_trinkets.bar_scan.repository.ShopRepository;
import com.juuls_trinkets.bar_scan.repository.UserRepository;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;

@Service
public class ShopService implements ShopApplicationService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    public ShopService(ShopRepository shopRepository, UserRepository userRepository) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ShopResponseDTO> findShops(CustomUserDetails user) {
        return shopRepository.findAllByCompanyId(user.getCompanyId()).stream()
                .map(ShopResponseDTO::new)
                .toList();
    }

    @Override
    public ShopResponseDTO createShop(CustomUserDetails user, CreateShopRequestDTO request) {
        try {
            Shop shop = shopRepository.create(new Shop(
                    UUID.randomUUID(),
                    user.getCompanyId(),
                    request.getName().trim()
            ));
            return new ShopResponseDTO(shop);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateResourceException("Shop already exists for company");
        }
    }

    @Override
    @Transactional
    public void removeShop(CustomUserDetails user, UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .filter(candidate -> candidate.getCompanyId().equals(user.getCompanyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        userRepository.deactivateByShopId(shop.getId());
        shopRepository.deactivate(shop.getId());
    }
}
