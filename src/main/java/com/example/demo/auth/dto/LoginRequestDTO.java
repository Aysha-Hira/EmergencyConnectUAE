//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}