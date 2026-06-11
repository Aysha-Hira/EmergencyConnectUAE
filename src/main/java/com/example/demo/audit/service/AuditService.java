//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.audit.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.audit.dto.AuditLogRequestDTO;
import com.example.demo.audit.dto.AuditLogResponseDTO;

public interface AuditService {
    
    // Log an action asynchronously
    void log(AuditLogRequestDTO request);
    
    // Convenience methods for common actions
    void logCreate(String userId, String username, String userRole, String entityType, String entityId, String details);
    
    void logUpdate(String userId, String username, String userRole, String entityType, String entityId, String oldValue, String newValue, String details);
    
    void logDelete(String userId, String username, String userRole, String entityType, String entityId, String details);
    
    void logStatusChange(String userId, String username, String userRole, String entityType, String entityId, String oldStatus, String newStatus, String details);
    
    void logAssignment(String userId, String username, String userRole, String entityType, String entityId, String unitId, String details);
    
    void logLogin(String userId, String username, String userRole, String ipAddress, boolean success, String errorMessage);
    
    void logMfa(String userId, String username, String userRole, String action, boolean success, String details);
    
    // Query methods
    Page<AuditLogResponseDTO> getAll(Pageable pageable);
    
    Page<AuditLogResponseDTO> getByUserId(String userId, Pageable pageable);
    
    Page<AuditLogResponseDTO> getByEntity(String entityType, String entityId, Pageable pageable);
    
    Page<AuditLogResponseDTO> getByAction(String action, Pageable pageable);
    
    Page<AuditLogResponseDTO> getByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    Page<AuditLogResponseDTO> search(String keyword, Pageable pageable);
}