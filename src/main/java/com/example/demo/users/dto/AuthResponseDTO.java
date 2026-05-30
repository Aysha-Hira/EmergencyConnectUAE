package com.example.demo.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String userId;
    private String username;
    private String role;
    private String token;   // returned so client can use it
    private String message;
}