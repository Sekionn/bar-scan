package com.juuls_trinkets.bar_scan.repository;

import java.util.Optional;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.model.AppUser;

public interface UserRepository {

    Optional<AppUser> findById(UUID id);

    Optional<AppUser> findByUsername(String username);

    long countByCompanyId(UUID companyId);

    AppUser create(AppUser user);
}
