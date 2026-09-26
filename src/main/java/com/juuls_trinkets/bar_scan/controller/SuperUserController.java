package com.juuls_trinkets.bar_scan.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.juuls_trinkets.bar_scan.dto.SetUserPasswordRequestDTO;
import com.juuls_trinkets.bar_scan.dto.UserResponseDTO;
import com.juuls_trinkets.bar_scan.service.superuser.SuperUserApplicationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/super/users")
public class SuperUserController {

    private final SuperUserApplicationService superUserService;

    public SuperUserController(SuperUserApplicationService superUserService) {
        this.superUserService = superUserService;
    }

    @PutMapping("/{userId}/password")
    public UserResponseDTO setUserPassword(
            @PathVariable UUID userId,
            @Valid @RequestBody SetUserPasswordRequestDTO request
    ) {
        return superUserService.setUserPassword(userId, request);
    }
}
