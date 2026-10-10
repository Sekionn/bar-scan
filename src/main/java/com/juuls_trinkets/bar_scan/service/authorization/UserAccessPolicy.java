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
    private static final String ROLE_DATA_ANALYST = "ROLE_DATA_ANALYST";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String OPTION_COMPANY_MANAGER = "company_manager";
    private static final String OPTION_SHOP_ADMIN = "shop_admin";
    private static final String OPTION_MOBILE_USER = "mobile_user";
    private static final String OPTION_DATA_ANALYST = "data_analyst";
    private static final String OPTION_SCANNER_USER = "scanner_user";

    public AuthCapabilitiesDTO capabilities(String role) {
        return new AuthCapabilitiesDTO(
                canUseWebData(role),
                canViewShops(role),
                canManageShops(role),
                canViewUsers(role),
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
                roleOption(OPTION_MOBILE_USER, ROLE_USER),
                roleOption(OPTION_DATA_ANALYST, ROLE_DATA_ANALYST)
        );
    }

    public String roleLabel(String role) {
        return switch (role) {
            case ROLE_USER -> "Mobile user";
            case ROLE_DATA_ANALYST -> "Data analyst";
            case ROLE_ADMIN -> "Company admin";
            case ROLE_MANAGER -> "Company manager";
            case ROLE_SUPER_ADMIN -> "System administrator";
            default -> "Unknown user type";
        };
    }

    public String normalizeAssignableRole(String role) {
        if (role == null || role.isBlank()) {
            return ROLE_USER;
        }
        return switch (role) {
            case OPTION_MOBILE_USER, OPTION_SCANNER_USER -> ROLE_USER;
            case OPTION_DATA_ANALYST -> ROLE_DATA_ANALYST;
            case OPTION_SHOP_ADMIN -> ROLE_ADMIN;
            case OPTION_COMPANY_MANAGER -> ROLE_MANAGER;
            case ROLE_USER, ROLE_DATA_ANALYST, ROLE_ADMIN, ROLE_MANAGER -> role;
            default -> throw new BadRequestException("Unsupported user role");
        };
    }

    public boolean canViewVisibleUsers(String role) {
        return canViewUsers(role);
    }

    public boolean canAssignShopAndRole(String role) {
        return ROLE_ADMIN.equals(role) || ROLE_MANAGER.equals(role) || ROLE_SUPER_ADMIN.equals(role);
    }

    public boolean canViewShops(String role) {
        return ROLE_DATA_ANALYST.equals(role) || canManageShops(role);
    }

    public boolean requiresShop(String role) {
        return ROLE_USER.equals(role);
    }

    private UserRoleOptionDTO roleOption(String value, String role) {
        return new UserRoleOptionDTO(value, roleLabel(role), requiresShop(role));
    }

    private boolean canUseWebData(String role) {
        return ROLE_DATA_ANALYST.equals(role)
                || ROLE_ADMIN.equals(role)
                || ROLE_MANAGER.equals(role)
                || ROLE_SUPER_ADMIN.equals(role);
    }

    private boolean canManageShops(String role) {
        return ROLE_ADMIN.equals(role) || ROLE_MANAGER.equals(role) || ROLE_SUPER_ADMIN.equals(role);
    }

    private boolean canManageUsers(String role) {
        return ROLE_ADMIN.equals(role) || ROLE_MANAGER.equals(role) || ROLE_SUPER_ADMIN.equals(role);
    }

    private boolean canViewUsers(String role) {
        return ROLE_DATA_ANALYST.equals(role) || canManageUsers(role);
    }

    private boolean canAccessSuperUserPassword(String role) {
        return ROLE_SUPER_ADMIN.equals(role);
    }
}
