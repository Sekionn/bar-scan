package com.juuls_trinkets.bar_scan.service.superuser;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juuls_trinkets.bar_scan.dto.SetUserPasswordRequestDTO;
import com.juuls_trinkets.bar_scan.dto.UserResponseDTO;
import com.juuls_trinkets.bar_scan.repository.UserRepository;

@Service
public class SuperUserService implements SuperUserApplicationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SuperUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public UserResponseDTO setUserPassword(UUID userId, SetUserPasswordRequestDTO request) {
        return new UserResponseDTO(userRepository.updatePassword(
                userId,
                passwordEncoder.encode(request.getPassword())
        ));
    }
}
