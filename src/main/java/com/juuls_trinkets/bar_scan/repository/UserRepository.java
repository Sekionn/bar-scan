package com.juuls_trinkets.bar_scan.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.model.AppUser;

public interface UserRepository {

    Optional<AppUser> findById(UUID id);

    Optional<AppUser> findByUsername(String username);

    List<AppUser> findAllByCompanyId(UUID companyId);

    List<AppUser> findAllByShopId(UUID shopId);

    long countByCompanyId(UUID companyId);

    long countByUsernamePrefix(String usernamePrefix);

    AppUser create(AppUser user);

    AppUser updatePassword(UUID userId, String passwordHash);

    void deactivate(UUID userId);
}
