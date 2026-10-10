package com.juuls_trinkets.bar_scan.dto;

public record AuthCapabilitiesDTO(
        boolean canUseWebData,
        boolean canViewShops,
        boolean canManageShops,
        boolean canViewUsers,
        boolean canManageUsers,
        boolean canAccessSuperUserPassword,
        boolean canAssignShopAndRole
) {
}
