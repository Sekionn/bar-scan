package com.juuls_trinkets.bar_scan.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateShopRequestDTO {

    @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
