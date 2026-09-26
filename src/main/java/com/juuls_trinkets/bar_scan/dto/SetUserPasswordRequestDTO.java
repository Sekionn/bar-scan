package com.juuls_trinkets.bar_scan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SetUserPasswordRequestDTO {

    @NotBlank
    @Size(min = 8)
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
