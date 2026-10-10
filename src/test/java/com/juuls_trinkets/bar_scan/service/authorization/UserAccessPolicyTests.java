package com.juuls_trinkets.bar_scan.service.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.juuls_trinkets.bar_scan.dto.AuthCapabilitiesDTO;

class UserAccessPolicyTests {

    private final UserAccessPolicy userAccessPolicy = new UserAccessPolicy();

    @Test
    void dataAnalystCanUseWebDataButCannotManageUsersOrShops() {
        AuthCapabilitiesDTO capabilities = userAccessPolicy.capabilities("ROLE_DATA_ANALYST");

        assertThat(capabilities.canUseWebData()).isTrue();
        assertThat(capabilities.canViewUsers()).isTrue();
        assertThat(capabilities.canManageUsers()).isFalse();
        assertThat(capabilities.canViewShops()).isTrue();
        assertThat(capabilities.canManageShops()).isFalse();
        assertThat(capabilities.canAssignShopAndRole()).isFalse();
        assertThat(capabilities.canAccessSuperUserPassword()).isFalse();
    }
}
