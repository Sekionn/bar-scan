package com.juuls_trinkets.bar_scan.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ContactSignupRequestDTO {

    @NotBlank
    private String companyName;

    @NotBlank
    private String locationName;

    @Min(1)
    private int allowedUserCount;

    @Email
    @NotBlank
    private String adminEmail;

    private String message;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public int getAllowedUserCount() {
        return allowedUserCount;
    }

    public void setAllowedUserCount(int allowedUserCount) {
        this.allowedUserCount = allowedUserCount;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
