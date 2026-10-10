package com.juuls_trinkets.bar_scan.service.authorization;

import java.util.List;

import org.springframework.stereotype.Component;

import com.juuls_trinkets.bar_scan.dto.AuthCapabilitiesDTO;
import com.juuls_trinkets.bar_scan.dto.UserRoleOptionDTO;
import com.juuls_trinkets.bar_scan.exception.BadRequestException;

@Component
public class UserAccessPolicy {

    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String OPTION_COMPANY_MANAGER = "company_manager";
    private static final String OPTION_SHOP_ADMIN = "shop_admin";
    private static final String OPTION_SCANNER_USER = "scanner_user";

    public AuthCapabilitiesDTO capabilities(String role) {
        return new AuthCapabilitiesDTO(
                canUseWebData(role),
                canManageShops(role),
                canManageUsers(role),
                canAccessSuperUserPassword(role),
                canAssignShopAndRole(role)
        );
    }

    public List<UserRoleOptionDTO> assignableRoles(String role) {
        if (!canAssignShopAndRole(role)) {
            return List.of();
        }

        return List.of(
                roleOption(OPTION_SCANNER_USER, ROLE_USER),
                roleOption(OPTION_SHOP_ADMIN, ROLE_ADMIN),
                roleOption(OPTION_COMPANY_MANAGER, ROLE_MANAGER)
        );
    }

    public String roleLabel(String role) {
        return switch (role) {
            case ROLE_USER -> "Scannerbruger";
            case ROLE_ADMIN -> "Butiksadmin";
            case ROLE_MANAGER -> "Virksomhedsansvarlig";
            case ROLE_SUPER_ADMIN -> "Systemadministrator";
            default -> "Ukendt rolle";
        };
    }

    public String normalizeAssignableRole(String role) {
        if (role == null || role.isBlank()) {
            return ROLE_USER;
        }
        return switch (role) {
            case OPTION_SCANNER_USER -> ROLE_USER;
            case OPTION_SHOP_ADMIN -> ROLE_ADMIN;
            case OPTION_COMPANY_MANAGER -> ROLE_MANAGER;
            case ROLE_USER, ROLE_ADMIN, ROLE_MANAGER -> role;
            default -> throw new BadRequestException("Unsupported user role");
        };
    }

    public boolean canViewVisibleUsers(String role) {
        return ROLE_ADMIN.equals(role) || ROLE_MANAGER.equals(role) || ROLE_SUPER_ADMIN.equals(role);
    }

    public boolean canAssignShopAndRole(String role) {
        return ROLE_MANAGER.equals(role) || ROLE_SUPER_ADMIN.equals(role);
    }

    public boolean requiresShop(String role) {
        return ROLE_USER.equals(role) || ROLE_ADMIN.equals(role);
    }

    private UserRoleOptionDTO roleOption(String value, String role) {
        return new UserRoleOptionDTO(value, roleLabel(role), requiresShop(role));
    }

    private boolean canUseWebData(String role) {
        return ROLE_ADMIN.equals(role) || ROLE_MANAGER.equals(role) || ROLE_SUPER_ADMIN.equals(role);
    }

    private boolean canManageShops(String role) {
        return ROLE_MANAGER.equals(role) || ROLE_SUPER_ADMIN.equals(role);
    }

    private boolean canManageUsers(String role) {
        return canViewVisibleUsers(role);
    }

    private boolean canAccessSuperUserPassword(String role) {
        return ROLE_SUPER_ADMIN.equals(role);
    }
}
