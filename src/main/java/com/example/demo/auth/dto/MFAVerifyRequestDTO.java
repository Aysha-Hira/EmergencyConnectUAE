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