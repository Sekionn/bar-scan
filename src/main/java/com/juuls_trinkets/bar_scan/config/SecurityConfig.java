package com.juuls_trinkets.bar_scan.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/signup/**").permitAll()
                        .requestMatchers("/payments/dev/**").permitAll()
                        .requestMatchers("/super/**").hasRole("SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/shops", "/shops/**").hasAnyRole("DATA_ANALYST", "ADMIN", "MANAGER", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/shops").hasAnyRole("ADMIN", "MANAGER", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/shops/**").hasAnyRole("ADMIN", "MANAGER", "SUPER_ADMIN")
                        .requestMatchers("/shops/**").hasAnyRole("ADMIN", "MANAGER", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/auth/users").hasAnyRole("DATA_ANALYST", "ADMIN", "MANAGER", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/auth/users/**").hasAnyRole("ADMIN", "MANAGER", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/auth/users/**").hasAnyRole("ADMIN", "MANAGER", "SUPER_ADMIN")
                        .requestMatchers("/auth/register").hasAnyRole("ADMIN", "MANAGER", "SUPER_ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
