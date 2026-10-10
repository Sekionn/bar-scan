package com.juuls_trinkets.bar_scan.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.model.Shop;

public interface ShopRepository {

    List<Shop> findAllByCompanyId(UUID companyId);

    Optional<Shop> findById(UUID id);

    Shop create(Shop shop);

    void deactivate(UUID id);
}
