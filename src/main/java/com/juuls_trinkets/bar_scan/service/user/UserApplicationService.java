package com.juuls_trinkets.bar_scan.service.user;

import com.juuls_trinkets.bar_scan.dto.CreateUserRequestDTO;
import com.juuls_trinkets.bar_scan.dto.UserResponseDTO;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;

public interface UserApplicationService {

    UserResponseDTO registerUser(CreateUserRequestDTO request, CustomUserDetails creator);
}
