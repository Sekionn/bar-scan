package com.juuls_trinkets.bar_scan.dto;

import java.util.UUID;

public record ContactSignupResponseDTO(
        UUID signupId,
        UUID companyId,
        UUID adminUserId,
        String username,
        String contactStatus,
        String message
) {
}
