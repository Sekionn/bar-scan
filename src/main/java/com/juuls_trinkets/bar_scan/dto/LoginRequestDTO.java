package com.juuls_trinkets.bar_scan.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {

    public static final String CLIENT_TYPE_MOBILE = "mobile";
    public static final String CLIENT_TYPE_WEB = "web";

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String clientType = CLIENT_TYPE_MOBILE;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientType() {
        return clientType == null || clientType.isBlank()
                ? CLIENT_TYPE_MOBILE
                : clientType.trim().toLowerCase();
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }
}
