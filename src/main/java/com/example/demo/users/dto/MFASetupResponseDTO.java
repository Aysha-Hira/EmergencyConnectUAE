package com.example.demo.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MFASetupResponseDTO {
    private String secret;          // The secret key (save this)
    private String qrCodeUrl;       // URL to generate QR code from
    private String message;         // "Scan QR code or enter secret manually"
}