package com.juuls_trinkets.bar_scan.repository;

import java.util.Optional;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.model.Company;

public interface CompanyRepository {

    Optional<Company> findById(UUID id);
}
