package com.juuls_trinkets.bar_scan.service.user;

import java.util.List;

import com.juuls_trinkets.bar_scan.dto.CreateUserRequestDTO;
import com.juuls_trinkets.bar_scan.dto.UserResponseDTO;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;

public interface UserApplicationService {

    List<UserResponseDTO> findVisibleUsers(CustomUserDetails user);

    UserResponseDTO registerUser(CreateUserRequestDTO request, CustomUserDetails creator);
}
