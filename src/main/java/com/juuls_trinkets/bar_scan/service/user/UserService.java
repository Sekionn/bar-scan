package com.juuls_trinkets.bar_scan.service.user;

import java.text.Normalizer;
import java.util.List;
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
import com.juuls_trinkets.bar_scan.model.Shop;
import com.juuls_trinkets.bar_scan.repository.CompanyRepository;
import com.juuls_trinkets.bar_scan.repository.ShopRepository;
import com.juuls_trinkets.bar_scan.repository.UserRepository;
import com.juuls_trinkets.bar_scan.security.CustomUserDetails;
import com.juuls_trinkets.bar_scan.service.authorization.UserAccessPolicy;

@Service
public class UserService implements UserApplicationService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAccessPolicy userAccessPolicy;

    public UserService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            ShopRepository shopRepository,
            PasswordEncoder passwordEncoder,
            UserAccessPolicy userAccessPolicy
    ) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.shopRepository = shopRepository;
        this.passwordEncoder = passwordEncoder;
        this.userAccessPolicy = userAccessPolicy;
    }

    @Override
    public List<UserResponseDTO> findVisibleUsers(CustomUserDetails user) {
        String role = user.getAuthorities().iterator().next().getAuthority();
        if (userAccessPolicy.canAssignShopAndRole(role)) {
            return userRepository.findAllByCompanyId(user.getCompanyId()).stream()
                    .map(visibleUser -> new UserResponseDTO(visibleUser, userAccessPolicy))
                    .toList();
        }
        if ("ROLE_ADMIN".equals(role)) {
            UUID shopId = user.getShopId();
            if (shopId == null) {
                throw new ResourceNotFoundException("Admin user is not assigned to a shop");
            }
            return userRepository.findAllByShopId(shopId).stream()
                    .map(visibleUser -> new UserResponseDTO(visibleUser, userAccessPolicy))
                    .toList();
        }
        throw new BadRequestException("This user cannot manage users");
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
        } else if (userAccessPolicy.canAssignShopAndRole(creatorRole)) {
            targetRole = userAccessPolicy.normalizeAssignableRole(request.getRole());
            targetShopId = request.getShopId();
        }

        if (userAccessPolicy.requiresShop(targetRole)) {
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
        if (userRepository.countByCompanyId(company.getId()) >= company.getAllowedUserCount()) {
            throw new DuplicateResourceException("Company user limit has been reached");
        }

        String username = generateUsername(company, shopId);

        AppUser user = new AppUser(
                UUID.randomUUID(),
                username,
                passwordEncoder.encode(request.getPassword()),
                company.getId(),
                shopId,
                role
        );
        return new UserResponseDTO(userRepository.create(user), userAccessPolicy);
    }

    private String generateUsername(Company company, UUID shopId) {
        String shopIdentifier = shopId == null
                ? "company"
                : shopRepository.findById(shopId)
                        .map(Shop::getName)
                        .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        String prefix = trimUsernamePrefix(slug(company.getName()) + "-" + slug(shopIdentifier));
        long nextNumber = userRepository.countByUsernamePrefix(prefix) + 1;
        String username = prefix + "-user-" + nextNumber;

        while (userRepository.findByUsername(username).isPresent()) {
            nextNumber++;
            username = prefix + "-user-" + nextNumber;
        }

        return username;
    }

    private String trimUsernamePrefix(String prefix) {
        int suffixLength = "-user-999999".length();
        int maxPrefixLength = 100 - suffixLength;
        if (prefix.length() <= maxPrefixLength) {
            return prefix;
        }
        return prefix.substring(0, maxPrefixLength).replaceAll("-+$", "");
    }

    private String slug(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "unknown" : normalized;
    }
}
