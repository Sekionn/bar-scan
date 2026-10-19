package com.juuls_trinkets.bar_scan.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.juuls_trinkets.bar_scan.dto.LoginRequestDTO;
import com.juuls_trinkets.bar_scan.exception.BadRequestException;
import com.juuls_trinkets.bar_scan.exception.ForbiddenException;
import com.juuls_trinkets.bar_scan.model.AppUser;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;
import com.juuls_trinkets.bar_scan.security.JwtUtil;
import com.juuls_trinkets.bar_scan.service.authorization.UserAccessPolicy;
import com.juuls_trinkets.bar_scan.service.user.UserApplicationService;

class AuthenticationControllerTests {

    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final AuthenticationController controller = new AuthenticationController(
            authenticationManager,
            jwtUtil,
            new UserAccessPolicy(),
            mock(UserApplicationService.class)
    );

    @Test
    void rejectsAnAdministratorFromTheMobileApp() {
        LoginRequestDTO request = loginRequest(LoginRequestDTO.CLIENT_TYPE_MOBILE);
        Authentication authentication = authenticationForRole("ROLE_ADMIN");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        assertThatThrownBy(() -> controller.login(request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("This user does not have access to the mobile app.");

        verifyNoInteractions(jwtUtil);
    }

    @Test
    void rejectsAnUnsupportedClientType() {
        LoginRequestDTO request = loginRequest("unknown-client");
        Authentication authentication = authenticationForRole("ROLE_ADMIN");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        assertThatThrownBy(() -> controller.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Unsupported client type.");

        verifyNoInteractions(jwtUtil);
    }

    private LoginRequestDTO loginRequest(String clientType) {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("company-admin");
        request.setPassword("password");
        request.setClientType(clientType);
        return request;
    }

    private Authentication authenticationForRole(String role) {
        AppUser user = new AppUser(
                UUID.randomUUID(),
                "company-admin",
                "password-hash",
                UUID.randomUUID(),
                null,
                role
        );
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new CustomUserDetails(user));
        return authentication;
    }
}
