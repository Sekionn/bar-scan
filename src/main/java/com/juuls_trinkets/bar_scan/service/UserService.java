package com.juuls_trinkets.bar_scan.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juuls_trinkets.bar_scan.dto.CreateUserRequestDTO;
import com.juuls_trinkets.bar_scan.dto.UserResponseDTO;
import com.juuls_trinkets.bar_scan.exception.DuplicateResourceException;
import com.juuls_trinkets.bar_scan.exception.ResourceNotFoundException;
import com.juuls_trinkets.bar_scan.model.AppUser;
import com.juuls_trinkets.bar_scan.model.Company;
import com.juuls_trinkets.bar_scan.repository.CompanyRepository;
import com.juuls_trinkets.bar_scan.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDTO registerUser(CreateUserRequestDTO request, UUID companyId) {
        userRepository.findByUsername(request.getUsername()).ifPresent(user -> {
            throw new DuplicateResourceException("Username already exists");
        });

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (userRepository.countByCompanyId(company.getId()) >= company.getAllowedUserCount()) {
            throw new DuplicateResourceException("Company user limit has been reached");
        }

        AppUser user = new AppUser(
                UUID.randomUUID(),
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                company.getId(),
                "ROLE_USER"
        );
        return new UserResponseDTO(userRepository.create(user));
    }
}
