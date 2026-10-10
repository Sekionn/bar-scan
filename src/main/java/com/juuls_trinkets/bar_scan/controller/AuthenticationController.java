package com.juuls_trinkets.bar_scan.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juuls_trinkets.bar_scan.dto.AuthTokenResponseDTO;
import com.juuls_trinkets.bar_scan.dto.CreateUserRequestDTO;
import com.juuls_trinkets.bar_scan.dto.LoginRequestDTO;
import com.juuls_trinkets.bar_scan.dto.SetUserPasswordRequestDTO;
import com.juuls_trinkets.bar_scan.dto.UserResponseDTO;
import com.juuls_trinkets.bar_scan.exception.ForbiddenException;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;
import com.juuls_trinkets.bar_scan.security.JwtUtil;
import com.juuls_trinkets.bar_scan.service.authorization.UserAccessPolicy;
import com.juuls_trinkets.bar_scan.service.user.UserApplicationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserAccessPolicy userAccessPolicy;
    private final UserApplicationService userService;

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserAccessPolicy userAccessPolicy,
            UserApplicationService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userAccessPolicy = userAccessPolicy;
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<UserResponseDTO> users(@AuthenticationPrincipal CustomUserDetails user) {
        return userService.findVisibleUsers(user);
    }

    @PostMapping("/register")
    public UserResponseDTO register(
            @AuthenticationPrincipal CustomUserDetails creator,
            @Valid @RequestBody CreateUserRequestDTO request
    ) {
        return userService.registerUser(request, creator);
    }

    @PutMapping("/users/{userId}/password")
    public UserResponseDTO setUserPassword(
            @AuthenticationPrincipal CustomUserDetails actor,
            @PathVariable UUID userId,
            @Valid @RequestBody SetUserPasswordRequestDTO request
    ) {
        return userService.setUserPassword(userId, request, actor);
    }

    @DeleteMapping("/users/{userId}")
    public void removeUser(
            @AuthenticationPrincipal CustomUserDetails actor,
            @PathVariable UUID userId
    ) {
        userService.removeUser(userId, actor);
    }

    @PostMapping("/login")
    public AuthTokenResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        if (LoginRequestDTO.CLIENT_TYPE_WEB.equals(request.getClientType())
                && !userAccessPolicy.canUseWebData(user.getRole())) {
            throw new ForbiddenException("This user can only log in to the mobile app.");
        }

        return new AuthTokenResponseDTO(jwtUtil.generateToken((UserDetails) user));
    }

    @PostMapping("/refresh")
    public AuthTokenResponseDTO refresh(@AuthenticationPrincipal CustomUserDetails user) {
        return new AuthTokenResponseDTO(jwtUtil.generateToken(user));
    }

    @GetMapping("/me")
    public Map<String, String> me(Principal principal) {
        return Map.of("username", principal.getName());
    }
}
