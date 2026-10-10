package com.juuls_trinkets.bar_scan.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.juuls_trinkets.bar_scan.dto.AuthCapabilitiesDTO;
import com.juuls_trinkets.bar_scan.dto.UserRoleOptionDTO;
import com.juuls_trinkets.bar_scan.service.authorization.UserAccessPolicy;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    private final UserAccessPolicy userAccessPolicy;

    public JwtUtil(UserAccessPolicy userAccessPolicy) {
        this.userAccessPolicy = userAccessPolicy;
    }

    public String generateToken(UserDetails userDetails) {
        CustomUserDetails user = (CustomUserDetails) userDetails;
        String role = user.getAuthorities().iterator().next().getAuthority();

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId().toString())
                .claim("companyId", user.getCompanyId().toString())
                .claim("shopId", user.getShopId() == null ? null : user.getShopId().toString())
                .claim("role", role)
                .claim("capabilities", capabilitiesClaim(role))
                .claim("assignableRoles", assignableRolesClaim(role))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, Boolean> capabilitiesClaim(String role) {
        AuthCapabilitiesDTO capabilities = userAccessPolicy.capabilities(role);
        return Map.of(
                "canUseWebData", capabilities.canUseWebData(),
                "canViewShops", capabilities.canViewShops(),
                "canManageShops", capabilities.canManageShops(),
                "canViewUsers", capabilities.canViewUsers(),
                "canManageUsers", capabilities.canManageUsers(),
                "canAccessSuperUserPassword", capabilities.canAccessSuperUserPassword(),
                "canAssignShopAndRole", capabilities.canAssignShopAndRole()
        );
    }

    private List<Map<String, Object>> assignableRolesClaim(String role) {
        return userAccessPolicy.assignableRoles(role).stream()
                .map(this::assignableRoleClaim)
                .toList();
    }

    private Map<String, Object> assignableRoleClaim(UserRoleOptionDTO option) {
        return Map.of(
                "value", option.value(),
                "label", option.label(),
                "requiresShop", option.requiresShop()
        );
    }
}
