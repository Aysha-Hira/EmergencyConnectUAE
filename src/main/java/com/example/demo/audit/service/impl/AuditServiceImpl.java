//Section 104 - Group 4
//Aysha Hira(1088000), Fatima Syed(1095190), Laisa Isra(1089635)


package com.example.demo.audit.service.impl;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.audit.dto.AuditLogRequestDTO;
import com.example.demo.audit.dto.AuditLogResponseDTO;
import com.example.demo.audit.model.AuditLog;
import com.example.demo.audit.repository.AuditLogRepository;
import com.example.demo.audit.service.AuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void log(AuditLogRequestDTO request) {
        executor.submit(() -> {
            try {
                AuditLog auditLog = AuditLog.builder()
                    .userId(request.getUserId())
                    .username(request.getUsername())
                    .userRole(request.getUserRole())
                    .action(request.getAction())
                    .entityType(request.getEntityType())
                    .entityId(request.getEntityId())
                    .oldValue(request.getOldValue())
                    .newValue(request.getNewValue())
                    .details(request.getDetails())
                    .ipAddress(request.getIpAddress())
                    .timestamp(LocalDateTime.now())
                    .success(request.isSuccess())
                    .errorMessage(request.getErrorMessage())
                    .build();
                
                auditLogRepository.save(auditLog);
            } catch (Exception e) {
                log.error("Failed to save audit log: {}", e.getMessage());
            }
        });
    }

    @Override
    public void logCreate(String userId, String username, String userRole, String entityType, String entityId, String details) {
        log(AuditLogRequestDTO.builder()
            .userId(userId)
            .username(username)
            .userRole(userRole)
            .action("CREATE")
            .entityType(entityType)
            .entityId(entityId)
            .details(details)
            .success(true)
            .build());
    }

    @Override
    public void logUpdate(String userId, String username, String userRole, String entityType, String entityId, 
                          String oldValue, String newValue, String details) {
        log(AuditLogRequestDTO.builder()
            .userId(userId)
            .username(username)
            .userRole(userRole)
            .action("UPDATE")
            .entityType(entityType)
            .entityId(entityId)
            .oldValue(oldValue)
            .newValue(newValue)
            .details(details)
            .success(true)
            .build());
    }

    @Override
    public void logDelete(String userId, String username, String userRole, String entityType, String entityId, String details) {
        log(AuditLogRequestDTO.builder()
            .userId(userId)
            .username(username)
            .userRole(userRole)
            .action("DELETE")
            .entityType(entityType)
            .entityId(entityId)
            .details(details)
            .success(true)
            .build());
    }

    @Override
    public void logStatusChange(String userId, String username, String userRole, String entityType, String entityId, 
                                String oldStatus, String newStatus, String details) {
        log(AuditLogRequestDTO.builder()
            .userId(userId)
            .username(username)
            .userRole(userRole)
            .action("STATUS_CHANGE")
            .entityType(entityType)
            .entityId(entityId)
            .oldValue(oldStatus)
            .newValue(newStatus)
            .details(details)
            .success(true)
            .build());
    }

    @Override
    public void logAssignment(String userId, String username, String userRole, String entityType, String entityId, 
                              String unitId, String details) {
        log(AuditLogRequestDTO.builder()
            .userId(userId)
            .username(username)
            .userRole(userRole)
            .action("ASSIGN")
            .entityType(entityType)
            .entityId(entityId)
            .newValue(unitId)
            .details(details)
            .success(true)
            .build());
    }

    @Override
    public void logLogin(String userId, String username, String userRole, String ipAddress, boolean success, String errorMessage) {
        log(AuditLogRequestDTO.builder()
            .userId(userId)
            .username(username)
            .userRole(userRole)
            .action(success ? "LOGIN_SUCCESS" : "LOGIN_FAILED")
            .entityType("USER")
            .entityId(userId)
            .ipAddress(ipAddress)
            .details(success ? "User logged in successfully" : "Login failed: " + errorMessage)
            .success(success)
            .errorMessage(errorMessage)
            .build());
    }

    @Override
    public void logMfa(String userId, String username, String userRole, String action, boolean success, String details) {
        log(AuditLogRequestDTO.builder()
            .userId(userId)
            .username(username)
            .userRole(userRole)
            .action("MFA_" + action)
            .entityType("USER")
            .entityId(userId)
            .details(details)
            .success(success)
            .build());
    }

    @Override
    public Page<AuditLogResponseDTO> getAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Override
    public Page<AuditLogResponseDTO> getByUserId(String userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable).map(this::toResponseDTO);
    }

    @Override
    public Page<AuditLogResponseDTO> getByEntity(String entityType, String entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable).map(this::toResponseDTO);
    }

    @Override
    public Page<AuditLogResponseDTO> getByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable).map(this::toResponseDTO);
    }

    @Override
    public Page<AuditLogResponseDTO> getByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(start, end, pageable).map(this::toResponseDTO);
    }

    @Override
    public Page<AuditLogResponseDTO> search(String keyword, Pageable pageable) {
        return auditLogRepository.search(keyword, pageable).map(this::toResponseDTO);
    }
    
    private AuditLogResponseDTO toResponseDTO(AuditLog log) {
        return AuditLogResponseDTO.builder()
            .id(log.getId())
            .userId(log.getUserId())
            .username(log.getUsername())
            .userRole(log.getUserRole())
            .action(log.getAction())
            .entityType(log.getEntityType())
            .entityId(log.getEntityId())
            .details(log.getDetails())
            .ipAddress(log.getIpAddress())
            .timestamp(log.getTimestamp())
            .success(log.isSuccess())
            .errorMessage(log.getErrorMessage())
            .build();
    }
}