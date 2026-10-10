package com.juuls_trinkets.bar_scan.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juuls_trinkets.bar_scan.dto.CreateShopRequestDTO;
import com.juuls_trinkets.bar_scan.dto.ShopResponseDTO;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;
import com.juuls_trinkets.bar_scan.service.shop.ShopApplicationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/shops")
public class ShopController {

    private final ShopApplicationService shopService;

    public ShopController(ShopApplicationService shopService) {
        this.shopService = shopService;
    }

    @GetMapping
    public List<ShopResponseDTO> getShops(@AuthenticationPrincipal CustomUserDetails user) {
        return shopService.findShops(user);
    }

    @PostMapping
    public ShopResponseDTO createShop(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody CreateShopRequestDTO request
    ) {
        return shopService.createShop(user, request);
    }

    @DeleteMapping("/{shopId}")
    public void removeShop(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable UUID shopId
    ) {
        shopService.removeShop(user, shopId);
    }
}
