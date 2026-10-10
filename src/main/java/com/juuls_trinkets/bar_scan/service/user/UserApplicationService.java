package com.juuls_trinkets.bar_scan.service.user;

import java.util.List;
import java.util.UUID;

import com.juuls_trinkets.bar_scan.dto.CreateUserRequestDTO;
import com.juuls_trinkets.bar_scan.dto.SetUserPasswordRequestDTO;
import com.juuls_trinkets.bar_scan.dto.UserResponseDTO;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;

public interface UserApplicationService {

    List<UserResponseDTO> findVisibleUsers(CustomUserDetails user);

    UserResponseDTO registerUser(CreateUserRequestDTO request, CustomUserDetails creator);

    UserResponseDTO setUserPassword(UUID userId, SetUserPasswordRequestDTO request, CustomUserDetails actor);

    void removeUser(UUID userId, CustomUserDetails actor);
}
