package com.juuls_trinkets.bar_scan.service.superuser;

import java.util.UUID;

import com.juuls_trinkets.bar_scan.dto.SetUserPasswordRequestDTO;
import com.juuls_trinkets.bar_scan.dto.UserResponseDTO;

public interface SuperUserApplicationService {

    UserResponseDTO setUserPassword(UUID userId, SetUserPasswordRequestDTO request);
}
