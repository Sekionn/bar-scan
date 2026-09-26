package com.juuls_trinkets.bar_scan.service.shop;

import java.util.List;

import com.juuls_trinkets.bar_scan.dto.CreateShopRequestDTO;
import com.juuls_trinkets.bar_scan.dto.ShopResponseDTO;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;

public interface ShopApplicationService {

    List<ShopResponseDTO> findShops(CustomUserDetails user);

    ShopResponseDTO createShop(CustomUserDetails user, CreateShopRequestDTO request);
}
