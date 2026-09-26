package com.juuls_trinkets.bar_scan.service.user;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juuls_trinkets.bar_scan.dto.CreateUserRequestDTO;
import com.juuls_trinkets.bar_scan.dto.UserResponseDTO;
import com.juuls_trinkets.bar_scan.exception.BadRequestException;
import com.juuls_trinkets.bar_scan.exception.DuplicateResourceException;
import com.juuls_trinkets.bar_scan.exception.ResourceNotFoundException;
import com.juuls_trinkets.bar_scan.model.AppUser;
import com.juuls_trinkets.bar_scan.model.Company;
import com.juuls_trinkets.bar_scan.repository.CompanyRepository;
import com.juuls_trinkets.bar_scan.repository.ShopRepository;
import com.juuls_trinkets.bar_scan.repository.UserRepository;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;

@Service
public class UserService implements UserApplicationService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            ShopRepository shopRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.shopRepository = shopRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDTO registerUser(CreateUserRequestDTO request, UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        return registerUserForCompany(request, company, null, "ROLE_USER");
    }

    @Override
    @Transactional
    public UserResponseDTO registerUser(CreateUserRequestDTO request, CustomUserDetails creator) {
        Company company = companyRepository.findById(creator.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        String creatorRole = creator.getAuthorities().iterator().next().getAuthority();
        UUID targetShopId = creator.getShopId();
        String targetRole = "ROLE_USER";

        if ("ROLE_ADMIN".equals(creatorRole)) {
            if (targetShopId == null) {
                throw new ResourceNotFoundException("Admin user is not assigned to a shop");
            }
        } else if ("ROLE_MANAGER".equals(creatorRole) || "ROLE_SUPER_ADMIN".equals(creatorRole)) {
            targetRole = normalizeRequestedRole(request.getRole());
            targetShopId = request.getShopId();
        }

        if (requiresShop(targetRole)) {
            if (targetShopId == null) {
                throw new ResourceNotFoundException("Shop is required for this user role");
            }
            UUID finalTargetShopId = targetShopId;
            shopRepository.findById(finalTargetShopId)
                    .filter(shop -> shop.getCompanyId().equals(company.getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        } else {
            targetShopId = null;
        }

        return registerUserForCompany(request, company, targetShopId, targetRole);
    }

    private UserResponseDTO registerUserForCompany(CreateUserRequestDTO request, Company company, UUID shopId, String role) {
        userRepository.findByUsername(request.getUsername()).ifPresent(user -> {
            throw new DuplicateResourceException("Username already exists");
        });

        if (userRepository.countByCompanyId(company.getId()) >= company.getAllowedUserCount()) {
            throw new DuplicateResourceException("Company user limit has been reached");
        }

        AppUser user = new AppUser(
                UUID.randomUUID(),
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                company.getId(),
                shopId,
                role
        );
        return new UserResponseDTO(userRepository.create(user));
    }

    private String normalizeRequestedRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }
        return switch (role) {
            case "ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER" -> role;
            default -> throw new BadRequestException("Unsupported user role");
        };
    }

    private boolean requiresShop(String role) {
        return "ROLE_USER".equals(role) || "ROLE_ADMIN".equals(role);
    }
}
