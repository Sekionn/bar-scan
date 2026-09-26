package com.juuls_trinkets.bar_scan.service.contactsignup;

import com.juuls_trinkets.bar_scan.dto.ContactSignupRequestDTO;
import com.juuls_trinkets.bar_scan.dto.ContactSignupResponseDTO;

public interface ContactSignupApplicationService {

    ContactSignupResponseDTO requestContactSignup(ContactSignupRequestDTO request);
}
