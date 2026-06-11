//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.auth.dto;

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
    String sessionId;
    String token;
    private String message;
    private Boolean mfaRequired;
    
    // Constructor for normal login (without MFA flag)
    public AuthResponseDTO(String userId, String username, String role, String sessionId, String token, String message) {
    	this.userId  = userId;
    	this.username = username;
    	this.role = role;
    	this.token = token;
    	this.sessionId = sessionId;
    	this.message = message;
    	this.mfaRequired = false;
    }
    
    // Static factory method for MFA-required response
    public static AuthResponseDTO mfaRequired(String userId, String message) {
        AuthResponseDTO dto = new AuthResponseDTO();
        dto.setUserId(userId);
        dto.setMessage(message);
        dto.setMfaRequired(true);
        return dto;
    }
}