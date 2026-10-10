package com.juuls_trinkets.bar_scan.dto;

import java.util.UUID;

import com.juuls_trinkets.bar_scan.model.AppUser;
import com.juuls_trinkets.bar_scan.service.authorization.UserAccessPolicy;

public record UserResponseDTO(UUID id, String username, UUID companyId, UUID shopId, String roleLabel) {
    public UserResponseDTO(AppUser user, UserAccessPolicy userAccessPolicy) {
        this(
                user.getId(),
                user.getUsername(),
                user.getCompanyId(),
                user.getShopId(),
                userAccessPolicy.roleLabel(user.getRole())
        );
    }
}
