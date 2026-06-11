//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MFASetupResponseDTO {
    private String secret;          // The secret key (save this)
    private String qrCodeUrl;       // URL to generate QR code from
    private String message;         // "Scan QR code or enter secret manually"
}