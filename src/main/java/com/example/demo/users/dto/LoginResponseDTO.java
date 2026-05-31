package com.example.demo.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private boolean mfaRequired;    // If true, user must verify MFA first
    private String userId;          // Only sent if mfaRequired=true
    private String message;         // "Please verify MFA code"
    private String token;           // Only sent if mfaRequired=false (login complete)
    private String role;            // Only sent if mfaRequired=false
    private String username;        // Only sent if mfaRequired=false
}