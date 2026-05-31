package com.example.demo.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDTO {
    private String userId;
    private String username;
    private String role;
    private String token;
    private String message;
    private Boolean mfaRequired;  // NEW FIELD - tells client if MFA needed
    
    // Constructor for normal login (without MFA flag)
    public AuthResponseDTO(String userId, String username, String role, String token, String message) {
        this(userId, username, role, token, message, false);
    }
    
    // Static factory method for MFA-required response
    public static AuthResponseDTO mfaRequired(String userId, String message) {
        return new AuthResponseDTO(userId, null, null, null, message, true);
    }
}