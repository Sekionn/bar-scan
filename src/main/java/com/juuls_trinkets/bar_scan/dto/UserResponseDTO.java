package com.juuls_trinkets.bar_scan.dto;

import java.util.UUID;

import com.juuls_trinkets.bar_scan.model.AppUser;

public record UserResponseDTO(UUID id, String username, UUID companyId, String role) {
    public UserResponseDTO(AppUser user) {
        this(user.getId(), user.getUsername(), user.getCompanyId(), user.getRole());
    }
}
