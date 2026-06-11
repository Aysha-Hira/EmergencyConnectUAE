//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.auth.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MFAVerifyRequestDTO {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "6-digit code is required")
    @Min(value = 100000, message = "Code must be 6 digits")
    private Integer code;  // The 6-digit number from authenticator app
}