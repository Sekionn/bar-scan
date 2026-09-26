package com.juuls_trinkets.bar_scan.dto;

import java.util.UUID;

import com.juuls_trinkets.bar_scan.model.Company;

public record CompanyResponseDTO(UUID id, String name, int allowedUserCount, String locationName) {
    public CompanyResponseDTO(Company company) {
        this(company.getId(), company.getName(), company.getAllowedUserCount(), company.getLocationName());
    }
}
