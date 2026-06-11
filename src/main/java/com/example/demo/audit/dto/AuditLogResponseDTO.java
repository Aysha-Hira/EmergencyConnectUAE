//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.audit.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDTO {
    private String id;
    private String userId;
    private String username;
    private String userRole;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;
    private boolean success;
    private String errorMessage;
}