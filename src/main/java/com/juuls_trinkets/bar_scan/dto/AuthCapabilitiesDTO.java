package com.juuls_trinkets.bar_scan.dto;

public record AuthCapabilitiesDTO(
        boolean canUseWebData,
        boolean canManageShops,
        boolean canManageUsers,
        boolean canAccessSuperUserPassword,
        boolean canAssignShopAndRole
) {
}
